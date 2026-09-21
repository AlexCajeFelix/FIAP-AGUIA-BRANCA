package com.example.aguia_azul.data

import com.google.gson.annotations.SerializedName

/**
 * O contrato do backend, campo a campo.
 *
 * Estes tipos existem para nao misturar o que a API devolve com o que a tela mostra: dinheiro
 * chega como numero e status como enum, e so o mapper (ApiMappers.kt) transforma isso em texto
 * formatado. Conferido contra target/openapi.json.
 */

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RefreshRequestDto(
    val refreshToken: String
)

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    /** OPERADOR, GESTOR ou LIDERANCA — o perfil vem do servidor, nao da tela de selecao. */
    val role: String
)

data class IdeaRequestDto(
    val title: String,
    val description: String
)

data class IdeaReviewRequestDto(
    /** APPROVED ou REJECTED. DRAFT e recusado pelo dominio. */
    val status: String
)

data class IdeaResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val status: String,
    val ownerId: Long?,
    val createdAt: String?,
    val reviewedAt: String?
)

data class PromoteIdeaRequestDto(
    val budget: Double
)

data class MetricsPatchRequestDto(
    val progress: Int? = null,
    val spent: Double? = null
)

data class ProjectResponseDto(
    val id: Long,
    val name: String,
    val status: String,
    val progress: Int,
    val budget: Double,
    val spent: Double,
    val ideaId: Long?,
    val createdAt: String?
)

data class ProjectSummaryDto(
    val totalProjects: Long?,
    val avgProgress: Double?,
    val totalBudget: Double?
)

data class ProjectStatusCountDto(
    val status: String,
    val total: Long
)

data class DashboardResponseDto(
    val summary: ProjectSummaryDto,
    val byStatus: List<ProjectStatusCountDto>
)

data class StrategyRequestDto(
    val title: String,
    val description: String,
    /** SHORT, MEDIUM ou LONG. */
    val horizon: String
)

data class StrategyResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val horizon: String,
    val createdAt: String?
)

data class SuggestionRequestDto(
    val title: String,
    val draft: String
)

data class SuggestionResponseDto(
    val description: String
)

/**
 * RFC 7807. O app decide comportamento pelo type (que e contrato) e mostra o detail, que e
 * a frase pronta em portugues que o backend ja escreveu.
 */
data class ProblemDetailDto(
    val type: String?,
    val title: String?,
    val status: Int?,
    val detail: String?,
    @SerializedName("errors")
    val errors: Map<String, String>?
)
