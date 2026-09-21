package com.example.aguia_azul.ui.lideranca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguia_azul.data.ApiException
import com.example.aguia_azul.data.ApiMappers
import com.example.aguia_azul.data.ApiRepository
import com.example.aguia_azul.data.ProjectApiModel
import com.example.aguia_azul.data.StrategyApiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Uma fatia do grafico: um rotulo e um numero. */
data class DistributionSlice(
    val label: String,
    val value: Float
)

data class TrendEntry(
    val label: String,
    val value: Float,
    val timestamp: Long
)

data class LiderancaDashboardState(
    val totalBudgetLabel: String = "R$ 0,00",
    val activeProjectsLabel: String = "0",
    val totalSpentLabel: String = "R$ 0,00",
    val projectsCountLabel: String = "0",
    val statusDistribution: List<DistributionSlice> = emptyList(),
    val budgetTrend: List<TrendEntry> = emptyList()
)

class LiderancaViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {
    private val _strategies = MutableStateFlow<List<StrategyApiModel>>(emptyList())
    val strategies: StateFlow<List<StrategyApiModel>> = _strategies.asStateFlow()

    private val _projects = MutableStateFlow<List<ProjectApiModel>>(emptyList())
    val projects: StateFlow<List<ProjectApiModel>> = _projects.asStateFlow()

    private val _dashboardState = MutableStateFlow(LiderancaDashboardState())
    val dashboardState: StateFlow<LiderancaDashboardState> = _dashboardState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _strategies.value = repository.fetchEstrategias()
                val projects = repository.fetchProjetos()
                _projects.value = projects
                // Os totais vem do /projects/summary: quem soma e o banco, num $group so.
                // Somar no cliente daria numero diferente assim que a lista fosse paginada.
                val dashboard = repository.fetchDashboard()
                _dashboardState.value = LiderancaDashboardState(
                    totalBudgetLabel = ApiMappers.moneyCompact(dashboard.totalBudget),
                    activeProjectsLabel = dashboard.byStatus
                        .firstOrNull { it.label == com.example.aguia_azul.data.ProjectStatuses.IN_PRODUCTION }
                        ?.total?.toString() ?: "0",
                    totalSpentLabel = ApiMappers.moneyCompact(projects.sumOf { it.spentValue }),
                    projectsCountLabel = dashboard.totalProjects.toString(),
                    statusDistribution = dashboard.byStatus.map {
                        DistributionSlice(it.label, it.total.toFloat())
                    },
                    budgetTrend = buildBudgetTrend(projects))
                _loaded.value = true
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveStrategy(id: Int?, title: String, description: String, horizon: String = "MEDIUM") {
        mutate {
            _strategies.value = repository.saveStrategy(
                StrategyApiModel(
                    id = id ?: 0,
                    title = title.trim(),
                    description = description.trim(),
                    horizon = horizon))
        }
    }

    fun deleteStrategy(id: Int) {
        mutate { _strategies.value = repository.deleteStrategy(id) }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                block()
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Orcamento somado por data de criacao — a serie temporal que a API sustenta hoje. */
    private fun buildBudgetTrend(projects: List<ProjectApiModel>): List<TrendEntry> =
        projects
            .filter { it.date.isNotBlank() }
            .groupBy { it.date }
            .map { (date, group) ->
                TrendEntry(
                    label = date.take(5),
                    value = group.sumOf { it.budgetValue }.toFloat(),
                    timestamp = dayMonthYearToMillis(date))
            }
            .sortedBy { it.timestamp }

    private fun dayMonthYearToMillis(date: String): Long {
        val parts = date.split("/")
        if (parts.size != 3) {
            return 0L
        }
        val (day, month, year) = parts
        return (year + month + day).toLongOrNull() ?: 0L
    }
}
