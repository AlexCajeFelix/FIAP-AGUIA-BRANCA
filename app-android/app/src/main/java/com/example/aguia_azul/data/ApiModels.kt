package com.example.aguia_azul.data

import com.google.gson.annotations.SerializedName
import java.text.Normalizer

object ProjectStatuses {
    const val APPROVED = "Aprovado"
    const val REJECTED = "Rejeitado"
    const val IN_PRODUCTION = "Em Producao"
    const val FINALIZED = "Finalizado"
}

fun normalizeProjectStatus(rawStatus: String): String {
    val normalized = Normalizer.normalize(rawStatus.trim(), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()

    return when {
        normalized.contains("rejeit") -> ProjectStatuses.REJECTED
        normalized.contains("finaliz") || normalized.contains("conclu") -> ProjectStatuses.FINALIZED
        normalized.contains("produc") || normalized.contains("execu") -> ProjectStatuses.IN_PRODUCTION
        normalized.contains("aprova") -> ProjectStatuses.APPROVED
        else -> ProjectStatuses.APPROVED
    }
}

data class ProjectApiModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("status")
    val status: String = ProjectStatuses.APPROVED,
    @SerializedName("currentStage")
    val currentStage: String,
    @SerializedName("investment")
    val investment: String,
    @SerializedName("estimatedRoi")
    val estimatedRoi: String,
    @SerializedName("achievedRoi")
    val achievedRoi: String,
    @SerializedName("estimatedSavings")
    val estimatedSavings: String,
    @SerializedName("actualSavings")
    val actualSavings: String,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("manager")
    val manager: String,
    @SerializedName("impact")
    val impact: String,
    @SerializedName("creationDate")
    val creationDate: String,
    @SerializedName("progress")
    val progress: Float,
    /**
     * Os valores numericos como a API manda. As versoes em texto (investment, actualSavings)
     * servem so para exibir; conta feita sobre string formatada e onde "R$ 1,2 mi" vira zero.
     */
    val budgetValue: Double = 0.0,
    val spentValue: Double = 0.0,
    val progressPercent: Int = 0,
    val operatorName: String = "",
    val description: String = "",
    val date: String = "",
    @SerializedName("category")
    val category: String = ""
)

data class StrategyApiModel(
    val id: Int,
    val title: String,
    val description: String,
    val date: String = "",
    /** SHORT, MEDIUM ou LONG — o horizonte que a API exige ao criar ou editar. */
    val horizon: String = "MEDIUM"
)

data class IdeaApiModel(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val status: String,
    val progress: Float,
    val type: String,
    val unit: String,
    val category: String
)

data class ProjectProgress(
    val ideaId: String,
    val stage: String,
    val status: String,
    val achievedRoi: String,
    val actualSavings: String
)
