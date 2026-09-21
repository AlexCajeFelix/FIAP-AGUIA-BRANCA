package com.example.aguia_azul

import android.app.Application
import com.example.aguia_azul.data.ApiConfig

/**
 * Ponto unico de inicializacao.
 *
 * O TokenStore precisa de Context, e o interceptor precisa do TokenStore antes da primeira
 * chamada — inicializar na Activity deixaria a janela em que um ViewModel criado cedo demais
 * acessaria um cliente HTTP sem sessao.
 */
class AguiaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiConfig.initialize(this)
    }
}
