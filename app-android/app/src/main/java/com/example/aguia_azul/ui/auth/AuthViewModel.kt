package com.example.aguia_azul.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aguia_azul.data.ApiException
import com.example.aguia_azul.data.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Login de verdade.
 *
 * Antes a tela so conferia se os campos estavam preenchidos e entrava com o perfil escolhido
 * no card anterior — qualquer pessoa entrava como LIDERANCA. Agora o perfil vem no corpo da
 * resposta do /auth/login, assinado no token que o app vai mandar em toda chamada seguinte.
 */
class AuthViewModel(
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        if (_isLoading.value) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val role = repository.login(email, password)
                if (role.isBlank()) {
                    _errorMessage.value = "Perfil nao reconhecido para este usuario."
                } else {
                    onSuccess(role)
                }
            } catch (error: Exception) {
                _errorMessage.value = ApiException.from(error).userMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
