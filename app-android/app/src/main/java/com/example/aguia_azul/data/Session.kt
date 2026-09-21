package com.example.aguia_azul.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado da sessao visivel para a UI.
 *
 * Quando o refresh e recusado — expirado, ou revogado porque a familia caiu — quem descobre e
 * o Authenticator do OkHttp, no meio de uma chamada qualquer. Sem este aviso a tela ficaria
 * mostrando erro de carregamento sem dizer o que realmente aconteceu.
 */
object Session {

    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired.asStateFlow()

    fun markExpired() {
        _expired.value = true
    }

    fun consumeExpired() {
        _expired.value = false
    }

    /** "OPERADOR" -> "operador": e o que as rotas de navegacao usam. */
    fun routeRoleOf(apiRole: String?): String = when (apiRole?.uppercase()) {
        "OPERADOR" -> "operador"
        "GESTOR" -> "gestor"
        "LIDERANCA" -> "lideranca"
        else -> ""
    }
}
