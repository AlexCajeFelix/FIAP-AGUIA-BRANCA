package com.example.aguia_azul.data

import com.example.aguia_azul.ui.gestor.SubmittedIdea
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A traducao entre o contrato do backend e o que as telas mostram.
 *
 * Fica num arquivo so, e nao espalhado pelos ViewModels, porque e aqui que mora a parte
 * arbitraria: o backend tem quatro status e o app tem quatro rotulos, e a correspondencia
 * entre eles e decisao de produto, nao detalhe de rede.
 *
 * Campos que a tela pede e a API nao tem (unidade, gestor responsavel, ROI, economia,
 * categoria) recebem SEM_DADO. Inventar numero aqui seria pior do que mostrar o traco.
 */
object ApiMappers {

    const val SEM_DADO = "—"

    private val brl: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    private val diaMesAno = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())
    private val isoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())

    fun money(value: Double): String = brl.format(value)

    /**
     * "R$ 2,68 mi" no lugar de "R$ 2.680.000,00".
     *
     * Cartao de dashboard tem largura fixa: o valor por extenso era cortado no meio e a tela
     * mostrava "R$ 2.680.00", que le como dois mil e seiscentos.
     */
    fun moneyCompact(value: Double): String = when {
        value >= 1_000_000 -> "R$ %.2f mi".format(Locale.forLanguageTag("pt-BR"), value / 1_000_000)
        value >= 1_000 -> "R$ %.1f mil".format(Locale.forLanguageTag("pt-BR"), value / 1_000)
        else -> brl.format(value)
    }

    fun ProjectResponseDto.toUi(): ProjectApiModel = ProjectApiModel(
        id = id.toString(),
        title = name,
        status = projectStatusLabel(status),
        currentStage = projectStageLabel(status),
        investment = money(budget),
        estimatedRoi = SEM_DADO,
        achievedRoi = SEM_DADO,
        estimatedSavings = SEM_DADO,
        actualSavings = SEM_DADO,
        unit = SEM_DADO,
        manager = SEM_DADO,
        impact = SEM_DADO,
        creationDate = createdAt?.let { isoDate.format(Instant.parse(it)) }.orEmpty(),
        progress = progress / 100f,
        budgetValue = budget,
        spentValue = spent,
        progressPercent = progress,
        operatorName = SEM_DADO,
        description = "Orcamento ${money(budget)} · gasto ${money(spent)}",
        date = createdAt?.let { diaMesAno.format(Instant.parse(it)) }.orEmpty(),
        category = SEM_DADO
    )

    fun IdeaResponseDto.toUi(): IdeaApiModel = IdeaApiModel(
        id = id.toInt(),
        title = title,
        description = description,
        date = createdAt?.let { diaMesAno.format(Instant.parse(it)) }.orEmpty(),
        status = ideaStatusLabel(status),
        // O progresso da ideia e o estagio da revisao, nao percentual de obra.
        progress = when (status) {
            "DRAFT" -> 0.25f
            "IN_REVIEW" -> 0.5f
            else -> 1f
        },
        type = "Ideia",
        unit = SEM_DADO,
        category = SEM_DADO
    )

    fun IdeaResponseDto.toSubmitted(): SubmittedIdea = SubmittedIdea(
        id = id.toString(),
        // A API devolve o id do dono, nao o nome: nao ha endpoint de usuarios.
        operatorName = ownerId?.let { "Operador #$it" } ?: SEM_DADO,
        unit = SEM_DADO,
        title = title,
        description = description,
        date = createdAt?.let { diaMesAno.format(Instant.parse(it)) }.orEmpty()
    )

    fun StrategyResponseDto.toUi(): StrategyApiModel = StrategyApiModel(
        id = id.toInt(),
        title = title,
        description = description,
        date = createdAt?.let { diaMesAno.format(Instant.parse(it)) }.orEmpty(),
        horizon = horizon
    )

    fun projectStatusLabel(apiStatus: String): String = when (apiStatus) {
        "PLANNING" -> ProjectStatuses.APPROVED
        "IN_PROGRESS" -> ProjectStatuses.IN_PRODUCTION
        "COMPLETED" -> ProjectStatuses.FINALIZED
        "CANCELLED" -> ProjectStatuses.REJECTED
        else -> ProjectStatuses.APPROVED
    }

    private fun projectStageLabel(apiStatus: String): String = when (apiStatus) {
        "PLANNING" -> "Planejamento"
        "IN_PROGRESS" -> "Execucao"
        "COMPLETED" -> "Concluido"
        "CANCELLED" -> "Cancelado"
        else -> SEM_DADO
    }

    private fun ideaStatusLabel(apiStatus: String): String = when (apiStatus) {
        "APPROVED" -> "Aprovada"
        "REJECTED" -> "Recusada"
        else -> "Em analise"
    }

    /** DRAFT e IN_REVIEW sao o que ainda espera decisao do gestor. */
    fun isPending(apiStatus: String): Boolean = apiStatus == "DRAFT" || apiStatus == "IN_REVIEW"
}
