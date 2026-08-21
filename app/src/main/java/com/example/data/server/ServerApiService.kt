package com.example.data.server

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit REST Interface for communication with FastAPI backend.
 */
interface ServerApiService {

  @GET("api/health")
  suspend fun getHealth(
    @Header("X-API-Key") apiKey: String
  ): Response<HealthResponse>

  @POST("api/clients/register")
  suspend fun registerClient(
    @Header("X-API-Key") apiKey: String,
    @Body request: ClientRegisterRequest
  ): Response<ClientRegisterResponse>

  @POST("api/sync")
  suspend fun syncBatch(
    @Header("X-API-Key") apiKey: String,
    @Body request: BatchSyncRequest
  ): Response<BatchSyncResponse>

  @GET("api/database/stats")
  suspend fun getDatabaseStats(
    @Header("X-API-Key") apiKey: String
  ): Response<DatabaseStatsResponse>

  @GET("api/sessions")
  suspend fun getSessions(
    @Header("X-API-Key") apiKey: String
  ): Response<List<NetworkSessionDto>>

  @GET("api/sessions/{session_id}")
  suspend fun getSessionById(
    @Header("X-API-Key") apiKey: String,
    @Path("session_id") sessionId: String
  ): Response<NetworkSessionDto>
}
