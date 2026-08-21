package com.example.data.server

import android.content.Context
import android.os.Build
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
  DISCONNECTED,
  CONNECTING,
  CONNECTED,
  ERROR
}

/**
 * Manages HTTP/REST connectivity to the FastAPI + PostgreSQL backend.
 * Provides real connection verification, latency measurement, and health reporting.
 */
class ServerConnectionManager(
  private val context: Context,
  val configManager: ServerConfigManager
) {
  private val scope = CoroutineScope(Dispatchers.IO)

  private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
  val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

  private val _serverHealth = MutableStateFlow<HealthResponse?>(null)
  val serverHealth: StateFlow<HealthResponse?> = _serverHealth.asStateFlow()

  private val _lastPingLatencyMs = MutableStateFlow(0L)
  val lastPingLatencyMs: StateFlow<Long> = _lastPingLatencyMs.asStateFlow()

  private val _lastConnectionTimestamp = MutableStateFlow(0L)
  val lastConnectionTimestamp: StateFlow<Long> = _lastConnectionTimestamp.asStateFlow()

  private val _lastErrorMessage = MutableStateFlow<String?>(null)
  val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

  private val _isRegistered = MutableStateFlow(false)
  val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

  private var apiService: ServerApiService? = null
  private var currentBaseUrl: String = ""

  init {
    scope.launch {
      configManager.config.collect { cfg ->
        rebuildApiService(cfg)
      }
    }
  }

  private fun rebuildApiService(cfg: ServerConfig) {
    val baseUrl = cfg.baseUrl.let { if (it.endsWith("/")) it else "$it/" }
    if (baseUrl != currentBaseUrl || apiService == null) {
      try {
        currentBaseUrl = baseUrl
        val moshi = Moshi.Builder()
          .add(KotlinJsonAdapterFactory())
          .build()

        val logging = HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
          .connectTimeout(5, TimeUnit.SECONDS)
          .readTimeout(10, TimeUnit.SECONDS)
          .writeTimeout(10, TimeUnit.SECONDS)
          .addInterceptor(logging)
          .build()

        val retrofit = Retrofit.Builder()
          .baseUrl(baseUrl)
          .client(client)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()

        apiService = retrofit.create(ServerApiService::class.java)
      } catch (e: Exception) {
        Log.e("ServerConnectionManager", "Error building Retrofit client", e)
        _connectionStatus.value = ConnectionStatus.ERROR
        _lastErrorMessage.value = "Invalid URL format: ${e.message}"
      }
    }
  }

  suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
    val cfg = configManager.config.value
    rebuildApiService(cfg)
    val service = apiService ?: return@withContext false

    _connectionStatus.value = ConnectionStatus.CONNECTING
    _lastErrorMessage.value = null
    val startTime = System.currentTimeMillis()

    try {
      val response = service.getHealth(cfg.apiKey)
      val latency = System.currentTimeMillis() - startTime
      _lastPingLatencyMs.value = latency

      if (response.isSuccessful && response.body() != null) {
        val health = response.body()!!
        _serverHealth.value = health
        _connectionStatus.value = ConnectionStatus.CONNECTED
        _lastConnectionTimestamp.value = System.currentTimeMillis()
        _lastErrorMessage.value = null

        // Auto-register client
        registerClientInternal(service, cfg)
        true
      } else {
        val errorBody = response.errorBody()?.string() ?: "HTTP ${response.code()}"
        _connectionStatus.value = ConnectionStatus.ERROR
        _lastErrorMessage.value = "Server returned error: $errorBody"
        false
      }
    } catch (e: Exception) {
      Log.e("ServerConnectionManager", "Connection test failed", e)
      _connectionStatus.value = ConnectionStatus.ERROR
      _lastErrorMessage.value = "Failed to connect to ${cfg.baseUrl}: ${e.localizedMessage ?: e.message}"
      false
    }
  }

  suspend fun registerClient(): Boolean = withContext(Dispatchers.IO) {
    val cfg = configManager.config.value
    val service = apiService ?: return@withContext false
    registerClientInternal(service, cfg)
  }

  private suspend fun registerClientInternal(service: ServerApiService, cfg: ServerConfig): Boolean {
    return try {
      val req = ClientRegisterRequest(
        clientId = cfg.clientId,
        clientName = cfg.clientName,
        ipAddress = "Client Direct",
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        appVersion = "1.0-NT04",
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
      )
      val response = service.registerClient(cfg.apiKey, req)
      if (response.isSuccessful) {
        _isRegistered.value = true
        true
      } else {
        _isRegistered.value = false
        false
      }
    } catch (e: Exception) {
      Log.e("ServerConnectionManager", "Failed to register client", e)
      _isRegistered.value = false
      false
    }
  }

  fun disconnect() {
    _connectionStatus.value = ConnectionStatus.DISCONNECTED
    _serverHealth.value = null
    _lastErrorMessage.value = null
  }

  fun getApiService(): ServerApiService? = apiService
}
