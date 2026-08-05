package com.example.backend.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {

    // 1. Authentication
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/v1/auth/password-reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequestDto): Response<ApiResponse<Boolean>>

    @POST("api/v1/auth/reset-password")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirmRequestDto): Response<ApiResponse<Boolean>>


    // 2. Profile
    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfileDto>>

    @PUT("api/v1/user/profile")
    suspend fun updateUserProfile(@Body profile: UserProfileUpdateDto): Response<ApiResponse<UserProfileDto>>

    // 3. Settings
    @GET("api/v1/user/settings")
    suspend fun getUserSettings(): Response<ApiResponse<UserSettingsDto>>

    @PUT("api/v1/user/settings")
    suspend fun updateUserSettings(@Body settings: UserSettingsUpdateDto): Response<ApiResponse<UserSettingsDto>>

    @POST("api/v1/user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordDto): Response<ApiResponse<Boolean>>

    @retrofit2.http.HTTP(method = "DELETE", path = "api/v1/user/account", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountDto): Response<ApiResponse<Boolean>>

    // 4. Chat AI & Conversations
    @POST("api/v1/chat")
    suspend fun sendChatMessage(@Body request: ChatRequestDto): Response<ApiResponse<ChatResponseDto>>

    @Streaming
    @POST("api/v1/chat/stream")
    suspend fun sendChatMessageStream(@Body request: ChatRequestDto): Response<ResponseBody>

    @GET("api/v1/chat/conversations")
    suspend fun getConversations(@Query("q") query: String? = null): Response<ApiResponse<List<ConversationDto>>>

    @POST("api/v1/chat/conversations")
    suspend fun createConversation(@Body request: CreateConversationRequestDto): Response<ApiResponse<ConversationDto>>

    @GET("api/v1/chat/conversations/{id}")
    suspend fun getConversationDetail(@Path("id") id: String): Response<ApiResponse<ConversationDetailDto>>

    @PUT("api/v1/chat/conversations/{id}")
    suspend fun updateConversation(
        @Path("id") id: String,
        @Body request: UpdateConversationRequestDto
    ): Response<ApiResponse<ConversationDto>>

    @DELETE("api/v1/chat/conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String): Response<ApiResponse<Boolean>>

    @DELETE("api/v1/chat/conversations/{id}/messages")
    suspend fun clearConversationMessages(@Path("id") id: String): Response<ApiResponse<Boolean>>

    // 5. Image Generator
    @POST("api/v1/image/generate")
    suspend fun generateImage(@Body request: ImageGenRequestDto): Response<ApiResponse<ImageGenResponseDto>>

    @POST("api/v1/image/enhance-prompt")
    suspend fun enhancePrompt(@Body request: EnhancePromptRequestDto): Response<ApiResponse<EnhancePromptResponseDto>>

    @POST("api/v1/image/jobs")
    suspend fun createImageJob(@Body request: ImageGenRequestDto): Response<ApiResponse<ImageJobResponseDto>>

    @GET("api/v1/image/jobs/{jobId}")
    suspend fun getImageJobStatus(@Path("jobId") jobId: String): Response<ApiResponse<ImageJobResponseDto>>

    @DELETE("api/v1/image/jobs/{jobId}")
    suspend fun cancelImageJob(@Path("jobId") jobId: String): Response<ApiResponse<Boolean>>

    @GET("api/v1/image/history")
    suspend fun getImageHistory(): Response<ApiResponse<List<ImageGenResponseDto>>>

    @DELETE("api/v1/image/{imageId}")
    suspend fun deleteImage(@Path("imageId") imageId: String): Response<ApiResponse<Boolean>>

    // 6. Music Composer
    @POST("api/v1/music/generate")
    suspend fun generateMusic(@Body request: MusicGenRequestDto): Response<ApiResponse<MusicTrackResponseDto>>

    @POST("api/v1/music/enhance-prompt")
    suspend fun enhanceMusicPrompt(@Body request: EnhanceMusicPromptRequestDto): Response<ApiResponse<EnhanceMusicPromptResponseDto>>

    @POST("api/v1/music/jobs")
    suspend fun createMusicJob(@Body request: MusicGenRequestDto): Response<ApiResponse<MusicJobResponseDto>>

    @GET("api/v1/music/jobs/{jobId}")
    suspend fun getMusicJobStatus(@Path("jobId") jobId: String): Response<ApiResponse<MusicJobResponseDto>>

    @DELETE("api/v1/music/jobs/{jobId}")
    suspend fun cancelMusicJob(@Path("jobId") jobId: String): Response<ApiResponse<Boolean>>

    @GET("api/v1/music/history")
    suspend fun getMusicHistory(): Response<ApiResponse<List<MusicTrackResponseDto>>>

    @DELETE("api/v1/music/{trackId}")
    suspend fun deleteMusicTrack(@Path("trackId") trackId: String): Response<ApiResponse<Boolean>>

    @POST("api/v1/music/{trackId}/save")
    suspend fun toggleSaveMusicTrack(@Path("trackId") trackId: String): Response<ApiResponse<Boolean>>

    // 7. Game Mind General
    @GET("api/v1/games")
    suspend fun getGameOverview(): Response<ApiResponse<Map<String, Any>>>

    // 8. Chess
    @POST("api/v1/games/chess/move")
    suspend fun processChessMove(@Body request: ChessMoveRequestDto): Response<ApiResponse<GameResponseDto>>

    // 9. Tic-Tac-Toe
    @POST("api/v1/games/tictactoe/move")
    suspend fun processTicTacToeMove(@Body request: TicTacToeMoveRequestDto): Response<ApiResponse<GameResponseDto>>

    // 10. Maze
    @POST("api/v1/games/maze/generate")
    suspend fun generateMaze(@Body request: MazeRequestDto): Response<ApiResponse<GameResponseDto>>

    // 11. History
    @GET("api/v1/history")
    suspend fun getHistory(
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("sort") sort: String = "newest",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<ApiResponse<HistoryListDto>>

    @POST("api/v1/history")
    suspend fun createHistoryItem(@Body item: HistoryItemCreateDto): Response<ApiResponse<HistoryItemReadDto>>

    @DELETE("api/v1/history/{history_id}")
    suspend fun deleteHistoryItem(@Path("history_id") historyId: String): Response<ApiResponse<Boolean>>

    @DELETE("api/v1/history")
    suspend fun clearAllHistory(): Response<ApiResponse<Int>>
}
