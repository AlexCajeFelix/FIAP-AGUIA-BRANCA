package com.example.aguia_azul.data

import android.content.Context
import android.util.Log
import com.example.aguia_azul.BuildConfig
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * O cliente HTTP do app.
 *
 * A URL vem do BuildConfig (debug aponta para o backend local, release para HTTPS). O token
 * entra por interceptor, e nao a mao em cada chamada: uma rota nova nasce autenticada sem
 * ninguem precisar lembrar.
 */
object ApiConfig {

    private const val TAG = "ApiConfig"

    val gson: Gson by lazy { Gson() }

    private lateinit var tokens: TokenStore

    /** Chamado uma vez, pelo Application. Sem isto o interceptor nao teria onde ler o token. */
    fun initialize(context: Context) {
        tokens = TokenStore(context.applicationContext)
    }

    val tokenStore: TokenStore get() = tokens

    private val authInterceptor = Interceptor { chain ->
        val token = tokens.accessToken
        val request = if (token == null || chain.request().isAuthRoute()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }

    /**
     * Troca o par quando o access expira, no meio da chamada que falhou.
     *
     * responseCount para o laco: se o proprio refresh devolver 401, o token nao vale mais e a
     * unica saida e voltar para o login. Reusar um refresh ja rotacionado derruba a familia
     * inteira no servidor, entao insistir seria pior do que desistir.
     */
    private val refreshAuthenticator = Authenticator { _: Route?, response: okhttp3.Response ->
        if (response.request.isAuthRoute() || responseCount(response) >= 2) {
            return@Authenticator null
        }
        val refresh = tokens.refreshToken ?: return@Authenticator null

        synchronized(this) {
            val current = tokens.accessToken
            // Outra requisicao pode ter renovado enquanto esta esperava o lock.
            if (current != null && response.request.header("Authorization") != "Bearer $current") {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $current")
                    .build()
            }
            val renewed = runCatching { refreshApi.refresh(RefreshRequestDto(refresh)).execute() }
                .getOrNull()
            val body = renewed?.body()
            if (renewed == null || !renewed.isSuccessful || body == null) {
                Log.w(TAG, "Refresh recusado; encerrando a sessao")
                tokens.clear()
                Session.markExpired()
                return@Authenticator null
            }
            tokens.save(body)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${body.accessToken}")
                .build()
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .authenticator(refreshAuthenticator)
            .apply {
                if (BuildConfig.DEBUG) {
                    // Corpo completo so em debug: em release isso vazaria token no logcat.
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val service: AguiaBrancaApiService by lazy { retrofit.create(AguiaBrancaApiService::class.java) }

    /**
     * Cliente separado, sem interceptor nem authenticator: o refresh nao pode carregar o
     * access vencido nem disparar outro refresh.
     */
    private val refreshApi: RefreshApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(OkHttpClient.Builder().callTimeout(15, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(RefreshApi::class.java)
    }

    private fun Request.isAuthRoute(): Boolean {
        val path = url.encodedPath
        return path.endsWith("/auth/login") || path.endsWith("/auth/refresh")
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var previous = response.priorResponse
        while (previous != null) {
            count++
            previous = previous.priorResponse
        }
        return count
    }

    private interface RefreshApi {
        @POST("auth/refresh")
        fun refresh(@Body request: RefreshRequestDto): Call<TokenResponseDto>
    }
}
