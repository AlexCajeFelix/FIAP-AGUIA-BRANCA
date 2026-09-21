package com.example.aguia_azul.ui.gestor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguia_azul.data.ApiException
import com.example.aguia_azul.data.ApiRepository
import com.example.aguia_azul.data.ProjectApiModel
import com.example.aguia_azul.data.ProjectStatuses
import com.example.aguia_azul.data.StrategyApiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmittedIdea(
    val id: String,
    val operatorName: String,
    val unit: String,
    val title: String,
    val description: String,
    val date: String
)

class GestorViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {
    private val _pipeline = MutableStateFlow<List<SubmittedIdea>>(emptyList())
    val pipeline: StateFlow<List<SubmittedIdea>> = _pipeline.asStateFlow()

    private val _projects = MutableStateFlow<List<ProjectApiModel>>(emptyList())
    val projects: StateFlow<List<ProjectApiModel>> = _projects.asStateFlow()

    private val _activeProjects = MutableStateFlow<List<ProjectApiModel>>(emptyList())
    val activeProjects: StateFlow<List<ProjectApiModel>> = _activeProjects.asStateFlow()

    private val _strategies = MutableStateFlow<List<StrategyApiModel>>(emptyList())
    val strategies: StateFlow<List<StrategyApiModel>> = _strategies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSavingProject = MutableStateFlow(false)
    val isSavingProject: StateFlow<Boolean> = _isSavingProject.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _pipeline.value = repository.fetchPipeline()
                refreshProjectsState()
                _strategies.value = repository.fetchEstrategias()
                _loaded.value = true
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getIdeaById(id: String): SubmittedIdea? = _pipeline.value.find { it.id == id }

    fun getProjectById(id: String): ProjectApiModel? = _projects.value.find { it.id == id }

    fun approveIdea(id: String, onSuccess: () -> Unit = {}) {
        mutate(onSuccess) { _pipeline.value = repository.approveIdea(id) }
    }

    fun rejectIdea(id: String, onSuccess: () -> Unit = {}) {
        mutate(onSuccess) { _pipeline.value = repository.rejectIdea(id) }
    }

    /**
     * Ideia aprovada vira projeto com orcamento. A API recusa promover ideia nao aprovada e
     * recusa promover duas vezes — as duas respostas chegam aqui como mensagem pronta.
     */
    fun promoteIdea(ideaId: String, budget: Double, onSuccess: () -> Unit = {}) {
        mutate(onSuccess) {
            repository.promoteIdea(ideaId, budget)
            _pipeline.value = repository.fetchPipeline()
            refreshProjectsState()
        }
    }

    /** Progresso e gasto: o status do projeto e consequencia deles, nao campo editavel. */
    fun updateMetrics(projectId: String, progress: Int?, spent: Double?, onSuccess: () -> Unit = {}) {
        mutate(onSuccess) {
            repository.updateProjectMetrics(projectId, progress, spent)
            refreshProjectsState()
        }
    }

    private fun mutate(onSuccess: () -> Unit, block: suspend () -> Unit) {
        viewModelScope.launch {
            _isSavingProject.value = true
            _errorMessage.value = null
            try {
                block()
                onSuccess()
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isSavingProject.value = false
            }
        }
    }

    private suspend fun refreshProjectsState() {
        _projects.value = repository.fetchProjetos()
        _activeProjects.value = _projects.value.filter { it.status == ProjectStatuses.IN_PRODUCTION }
    }
}
