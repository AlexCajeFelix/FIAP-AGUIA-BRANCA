package com.example.aguia_azul.data

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

/**
 * Erro da API ja traduzido para algo que cabe na tela.
 *
 * O que existia antes era um catch que recarregava o mesmo mock e ainda assim mostrava
 * "nao foi possivel carregar": a pessoa via dado falso achando que era real. Agora a falha
 * sobe com a mensagem que o backend mandou.
 */
class ApiException(
    val statusCode: Int,
    /** URI do RFC 7807. E por ele que o app decide o que fazer, nunca pelo texto. */
    val type: String?,
    val userMessage: String
) : IOException(userMessage) {

    val isUnauthorized: Boolean get() = statusCode == 401

    val isSessionExpired: Boolean
        get() = type == ErrorTypes.INVALID_REFRESH || statusCode == 401

    object ErrorTypes {
        const val BASE = "https://aguiabranca.fiap.br/errors/"
        const val INVALID_CREDENTIALS = BASE + "credenciais-invalidas"
        const val INVALID_REFRESH = BASE + "refresh-invalido"
        const val RATE_LIMIT = BASE + "rate-limit-excedido"
        const val IDEA_ALREADY_REVIEWED = BASE + "ideia-ja-revisada"
        const val IDEA_ALREADY_PROMOTED = BASE + "ideia-ja-promovida"
        const val FORBIDDEN = BASE + "sem-permissao"
    }

    companion object {
        private val gson = Gson()

        fun from(response: Response<*>): ApiException {
            val body = response.errorBody()?.string().orEmpty()
            val problem = parse(body)
            return ApiException(
                statusCode = response.code(),
                type = problem?.type,
                userMessage = message(response.code(), problem))
        }

        fun from(error: Throwable): ApiException = when (error) {
            is ApiException -> error
            is HttpException -> ApiException(error.code(), null, message(error.code(), null))
            // Sem rede, o app precisa dizer isso e nao "erro inesperado": a acao do usuario
            // e diferente (ligar o wi-fi, conferir se o backend subiu).
            is IOException -> ApiException(0, null,
                "Sem conexao com o servidor. Confira se a API esta no ar.")
            else -> ApiException(-1, null, "Erro inesperado: ${error.message ?: error::class.simpleName}")
        }

        private fun parse(body: String): ProblemDetailDto? = try {
            if (body.isBlank()) null else gson.fromJson(body, ProblemDetailDto::class.java)
        } catch (_: JsonSyntaxException) {
            null
        }

        private fun message(statusCode: Int, problem: ProblemDetailDto?): String {
            val firstFieldError = problem?.errors?.values?.firstOrNull()
            val detail = problem?.detail?.takeIf { it.isNotBlank() }
            return detail
                ?: firstFieldError
                ?: when (statusCode) {
                    401 -> "Sessao expirada. Entre de novo."
                    403 -> "Seu perfil nao tem permissao para esta acao."
                    404 -> "Nao encontrado."
                    429 -> "Muitas tentativas. Aguarde um pouco."
                    in 500..599 -> "O servidor falhou. Tente de novo em instantes."
                    else -> "Nao foi possivel completar a operacao."
                }
        }
    }
}
