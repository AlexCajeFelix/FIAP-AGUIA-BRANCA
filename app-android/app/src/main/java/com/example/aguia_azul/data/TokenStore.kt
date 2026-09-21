package com.example.aguia_azul.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Onde a sessao fica entre aberturas do app.
 *
 * O par access/refresh vive aqui porque o access dura 30 minutos: sem guardar o refresh, o app
 * pediria login de novo a cada meia hora. Arquivo privado do app (MODE_PRIVATE) — nenhum outro
 * app le, mas tambem nao e cofre: em aparelho com root o arquivo e legivel. Para o escopo do
 * challenge isso basta; um passo adiante seria EncryptedSharedPreferences.
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aguia_sessao", Context.MODE_PRIVATE)

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        private set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        private set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    /** OPERADOR, GESTOR ou LIDERANCA, do jeito que o servidor devolveu. */
    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        private set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    val isLoggedIn: Boolean get() = accessToken != null

    fun save(tokens: TokenResponseDto) {
        prefs.edit()
            .putString(KEY_ACCESS, tokens.accessToken)
            .putString(KEY_REFRESH, tokens.refreshToken)
            .putString(KEY_ROLE, tokens.role)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_ROLE = "role"
    }
}
