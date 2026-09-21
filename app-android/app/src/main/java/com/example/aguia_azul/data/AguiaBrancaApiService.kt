package com.example.aguia_azul.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Os endpoints reais do Hub, no lugar dos tres ids do npoint.io.
 *
 * Tudo devolve Response<T> para o repositorio poder ler o corpo de erro (problem+json) em vez
 * de receber so um HttpException sem mensagem.
 */
interface AguiaBrancaApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): Response<TokenResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequestDto): Response<Unit>

    @GET("ideas")
    suspend fun listIdeas(@Query("status") status: String? = null): Response<List<IdeaResponseDto>>

    @GET("ideas/{id}")
    suspend fun getIdea(@Path("id") id: Long): Response<IdeaResponseDto>

    @POST("ideas")
    suspend fun createIdea(@Body request: IdeaRequestDto): Response<IdeaResponseDto>

    @POST("ideas/{id}/approval")
    suspend fun reviewIdea(
        @Path("id") id: Long,
        @Body request: IdeaReviewRequestDto
    ): Response<IdeaResponseDto>

    /** Só existe quando o servidor tem GEMINI_API_KEY; sem ela a rota responde 404. */
    @POST("ideas/suggest")
    suspend fun suggest(@Body request: SuggestionRequestDto): Response<SuggestionResponseDto>

    @GET("projects")
    suspend fun listProjects(): Response<List<ProjectResponseDto>>

    @GET("projects/summary")
    suspend fun dashboard(): Response<DashboardResponseDto>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") id: Long): Response<ProjectResponseDto>

    @POST("projects/from-idea/{ideaId}")
    suspend fun promoteIdea(
        @Path("ideaId") ideaId: Long,
        @Body request: PromoteIdeaRequestDto
    ): Response<ProjectResponseDto>

    @PATCH("projects/{id}/metrics")
    suspend fun updateMetrics(
        @Path("id") id: Long,
        @Body request: MetricsPatchRequestDto
    ): Response<ProjectResponseDto>

    @GET("strategies")
    suspend fun listStrategies(): Response<List<StrategyResponseDto>>

    @POST("strategies")
    suspend fun createStrategy(@Body request: StrategyRequestDto): Response<StrategyResponseDto>

    @PUT("strategies/{id}")
    suspend fun updateStrategy(
        @Path("id") id: Long,
        @Body request: StrategyRequestDto
    ): Response<StrategyResponseDto>

    @DELETE("strategies/{id}")
    suspend fun deleteStrategy(@Path("id") id: Long): Response<Unit>
}
