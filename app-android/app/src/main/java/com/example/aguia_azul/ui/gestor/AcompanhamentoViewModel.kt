package com.example.aguia_azul.ui.gestor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguia_azul.data.ApiException
import com.example.aguia_azul.data.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcompanhamentoViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** PATCH /projects/{id}/metrics: grava a metrica e o snapshot de auditoria na mesma transacao. */
    fun saveMetrics(projectId: String, progress: Int?, spent: Double?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                repository.updateProjectMetrics(projectId, progress, spent)
                onSuccess()
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isSaving.value = false
            }
        }
    }
}
