package com.nova.healthconnect.data.api

import com.nova.healthconnect.data.models.AiChatRequest
import com.nova.healthconnect.data.models.AiChatResponse
import com.nova.healthconnect.data.models.AuthResponse
import com.nova.healthconnect.data.models.CheckInRequest
import com.nova.healthconnect.data.models.CheckInResponse
import com.nova.healthconnect.data.models.DashboardResponse
import com.nova.healthconnect.data.models.FocusSessionRequest
import com.nova.healthconnect.data.models.FocusSessionResponse
import com.nova.healthconnect.data.models.HealthSyncRequest
import com.nova.healthconnect.data.models.HealthSyncResponse
import com.nova.healthconnect.data.models.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NovaApiService {

    @GET("dashboard")
    suspend fun getDashboard(
        @Query("date") date: String? = null
    ): Response<DashboardResponse>

    @POST("checkin")
    suspend fun submitCheckIn(
        @Body request: CheckInRequest
    ): Response<CheckInResponse>

    @POST("sessions")
    suspend fun recordFocusSession(
        @Body request: FocusSessionRequest
    ): Response<FocusSessionResponse>

    @POST("health-connect/sync")
    suspend fun syncHealthConnectData(
        @Body request: HealthSyncRequest
    ): Response<HealthSyncResponse>

    @POST("ai/chat")
    suspend fun sendAiChatMessage(
        @Body request: AiChatRequest
    ): Response<AiChatResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
}
