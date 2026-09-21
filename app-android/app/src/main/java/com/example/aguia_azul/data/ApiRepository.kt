package com.example.aguia_azul.data

import com.example.aguia_azul.data.ApiMappers.toSubmitted
import com.example.aguia_azul.data.ApiMappers.toUi
import com.example.aguia_azul.ui.gestor.SubmittedIdea
import com.example.aguia_azul.ui.operador.Idea
import com.example.aguia_azul.ui.operador.Strategy
import retrofit2.Response

/**
 * A unica porta de entrada do app para a API.
 *
 * O que mudou em relacao a versao de mocks: nao ha mais cache hardcoded nem fallback. Quando
 * a chamada falha, o erro sobe como ApiException com a mensagem que o backend escreveu, e a
 * tela mostra isso. Antes, o catch recarregava a mesma lista fixa e a pessoa via dado
 * inventado achando que estava vendo o sistema.
 */
class ApiRepository(
    private val apiService: AguiaBrancaApiService = ApiConfig.service,
    private val tokens: TokenStore = ApiConfig.tokenStore
) {

    /** Devolve o perfil ja no formato das rotas ("operador", "gestor", "lideranca"). */
    suspend fun login(email: String, password: String): String {
        val tokenResponse = apiService.login(LoginRequestDto(email.trim(), password)).orThrow()
        tokens.save(tokenResponse)
        Session.consumeExpired()
        return Session.routeRoleOf(tokenResponse.role)
    }

    suspend fun logout() {
        val refresh = tokens.refreshToken
        if (refresh != null) {
            // Falha aqui nao pode travar a saida: o que importa e apagar a sessao local.
            runCatching { apiService.logout(RefreshRequestDto(refresh)) }
        }
        tokens.clear()
    }

    suspend fun fetchProjetos(): List<ProjectApiModel> =
        apiService.listProjects().orThrow().map { it.toUi() }

    suspend fun fetchEstrategias(): List<StrategyApiModel> =
        apiService.listStrategies().orThrow().map { it.toUi() }

    suspend fun fetchIdeias(): List<IdeaApiModel> =
        apiService.listIdeas().orThrow().map { it.toUi() }

    suspend fun fetchDashboard(): DashboardSnapshot {
        val dashboard = apiService.dashboard().orThrow()
        return DashboardSnapshot(
            totalProjects = dashboard.summary.totalProjects ?: 0L,
            avgProgress = dashboard.summary.avgProgress ?: 0.0,
            totalBudget = dashboard.summary.totalBudget ?: 0.0,
            byStatus = dashboard.byStatus.map {
                StatusSlice(ApiMappers.projectStatusLabel(it.status), it.total)
            })
    }

    suspend fun fetchStrategies(): List<Strategy> =
        fetchEstrategias().mapIndexed { index, strategy ->
            Strategy(
                id = strategy.id,
                title = strategy.title,
                description = strategy.description,
                colorHex = CARD_COLORS[index % CARD_COLORS.size])
        }

    suspend fun fetchIdeas(): List<Idea> = fetchIdeias().map { it.toIdea() }

    suspend fun postIdea(title: String, description: String): List<Idea> {
        apiService.createIdea(IdeaRequestDto(title.trim(), description.trim())).orThrow()
        // Relê do servidor em vez de inserir na lista local: o id e a data vem de la, e uma
        // lista montada no cliente diverge da real no primeiro detalhe.
        return fetchIdeas()
    }

    /**
     * Reescreve o rascunho com apoio de IA. A chave do Gemini fica no servidor: embutida no
     * APK, qualquer pessoa extrairia com apktool e gastaria a cota.
     */
    suspend fun suggestDescription(title: String, draft: String): String {
        val response = apiService.suggest(SuggestionRequestDto(title.trim(), draft.trim()))
        if (response.code() == 404) {
            throw ApiException(404, null, "O assistente de IA nao esta habilitado no servidor.")
        }
        return response.orThrow().description
    }

    /** A fila do gestor e o que ainda nao foi decidido: DRAFT e IN_REVIEW. */
    suspend fun fetchPipeline(): List<SubmittedIdea> =
        apiService.listIdeas().orThrow()
            .filter { ApiMappers.isPending(it.status) }
            .map { it.toSubmitted() }

    suspend fun approveIdea(id: String): List<SubmittedIdea> = review(id, "APPROVED")

    suspend fun rejectIdea(id: String): List<SubmittedIdea> = review(id, "REJECTED")

    /** Ideia aprovada vira projeto com orcamento — a promocao e um passo separado da aprovacao. */
    suspend fun promoteIdea(ideaId: String, budget: Double): ProjectApiModel =
        apiService.promoteIdea(ideaId.toLongId(), PromoteIdeaRequestDto(budget)).orThrow().toUi()

    /**
     * O status do projeto nao e escrito direto: ele e consequencia do progresso (0 planeja,
     * 1..99 executa, 100 conclui). Por isso a tela de acompanhamento manda progresso e gasto.
     */
    suspend fun updateProjectMetrics(projectId: String, progress: Int?, spent: Double?): ProjectApiModel =
        apiService.updateMetrics(projectId.toLongId(), MetricsPatchRequestDto(progress, spent))
            .orThrow()
            .toUi()

    suspend fun saveStrategy(strategy: StrategyApiModel): List<StrategyApiModel> {
        val request = StrategyRequestDto(
            title = strategy.title.trim(),
            description = strategy.description.trim(),
            horizon = strategy.horizon)
        if (strategy.id > 0) {
            apiService.updateStrategy(strategy.id.toLong(), request).orThrow()
        } else {
            apiService.createStrategy(request).orThrow()
        }
        return fetchEstrategias()
    }

    suspend fun deleteStrategy(id: Int): List<StrategyApiModel> {
        apiService.deleteStrategy(id.toLong()).orThrow()
        return fetchEstrategias()
    }

    private suspend fun review(id: String, status: String): List<SubmittedIdea> {
        apiService.reviewIdea(id.toLongId(), IdeaReviewRequestDto(status)).orThrow()
        return fetchPipeline()
    }

    private fun IdeaApiModel.toIdea() = Idea(
        id = id,
        title = title,
        description = description,
        date = date,
        status = status,
        progress = progress,
        type = type,
        unit = unit,
        category = category)

    /**
     * Corpo vazio com sucesso (204 do DELETE e do logout) tambem passa: o Unit do Retrofit
     * chega como null e nao e erro.
     */
    private fun <T> Response<T>.orThrow(): T {
        if (!isSuccessful) {
            throw ApiException.from(this)
        }
        @Suppress("UNCHECKED_CAST")
        return body() ?: (Unit as T)
    }

    private fun String.toLongId(): Long =
        toLongOrNull() ?: throw ApiException(-1, null, "Identificador invalido: $this")

    private companion object {
        val CARD_COLORS = listOf(0xFFE1F5FE, 0xFFE3F2FD, 0xFFE8EAF6)
    }
}

/** Numeros do dashboard, como a API calcula — nada e somado no cliente. */
data class DashboardSnapshot(
    val totalProjects: Long,
    val avgProgress: Double,
    val totalBudget: Double,
    val byStatus: List<StatusSlice>
)

data class StatusSlice(
    val label: String,
    val total: Long
)
