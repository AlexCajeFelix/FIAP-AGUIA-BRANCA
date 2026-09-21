package com.example.aguia_azul.ui.operador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguia_azul.data.ApiException
import com.example.aguia_azul.data.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Strategy(
    val id: Int,
    val title: String,
    val description: String,
    val colorHex: Long
)

data class Idea(
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

class OperadorViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {
    private val _strategies = MutableStateFlow<List<Strategy>>(emptyList())
    val strategies: StateFlow<List<Strategy>> = _strategies.asStateFlow()

    private val _ideas = MutableStateFlow<List<Idea>>(emptyList())
    val ideas: StateFlow<List<Idea>> = _ideas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    /** Ja carregou ao menos uma vez sem erro — o "vazio" so e vazio depois disso. */
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _strategies.value = repository.fetchStrategies()
                _ideas.value = repository.fetchIdeas()
                _loaded.value = true
            } catch (error: Exception) {
                // Sem fallback: a tela mostra o erro e oferece tentar de novo. Recarregar um
                // mock aqui era o que fazia dado inventado passar por dado real.
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _isSuggesting = MutableStateFlow(false)
    val isSuggesting: StateFlow<Boolean> = _isSuggesting.asStateFlow()

    /** O texto volta para a tela; quem decide se usa e a pessoa, e nada e gravado antes disso. */
    fun improveDescription(title: String, draft: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isSuggesting.value = true
            _errorMessage.value = null
            try {
                onResult(repository.suggestDescription(title, draft))
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isSuggesting.value = false
            }
        }
    }

    fun addIdea(title: String, description: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            try {
                _ideas.value = repository.postIdea(title, description)
                onSuccess()
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
