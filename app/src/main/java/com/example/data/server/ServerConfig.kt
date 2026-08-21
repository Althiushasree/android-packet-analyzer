package com.example.data.server

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Configuration and persistent preferences for Client-Server synchronization.
 * Supports:
 * - Mode A: Same Device (e.g. 10.0.2.2 for emulator or 127.0.0.1 / localhost)
 * - Mode B: Two Different Laptops over LAN (e.g. 192.168.1.20:8000)
 */
data class ServerConfig(
  val serverHost: String = "10.0.2.2", // 10.0.2.2 for Android emulator -> host machine localhost:8000
  val serverPort: Int = 8000,
  val apiKey: String = "nt04-network-admin-secret-token",
  val clientId: String = "client-" + UUID.randomUUID().toString().take(8),
  val clientName: String = "${Build.MANUFACTURER} ${Build.MODEL}",
  val isAutoSyncEnabled: Boolean = true,
  val syncIntervalSeconds: Int = 15,
  val useHttps: Boolean = false
) {
  val baseUrl: String
    get() {
      val scheme = if (useHttps) "https" else "http"
      val cleanHost = serverHost.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
      return "$scheme://$cleanHost:$serverPort"
    }
}

class ServerConfigManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("nt04_server_config", Context.MODE_PRIVATE)

  private val _config = MutableStateFlow(loadConfig())
  val config: StateFlow<ServerConfig> = _config.asStateFlow()

  private fun loadConfig(): ServerConfig {
    val savedClientId = prefs.getString("client_id", null) ?: run {
      val newId = "CLIENT-" + UUID.randomUUID().toString().take(8).uppercase()
      prefs.edit().putString("client_id", newId).apply()
      newId
    }

    return ServerConfig(
      serverHost = prefs.getString("server_host", "10.0.2.2") ?: "10.0.2.2",
      serverPort = prefs.getInt("server_port", 8000),
      apiKey = prefs.getString("api_key", "nt04-network-admin-secret-token") ?: "nt04-network-admin-secret-token",
      clientId = savedClientId,
      clientName = prefs.getString("client_name", "${Build.MANUFACTURER} ${Build.MODEL}") ?: "${Build.MANUFACTURER} ${Build.MODEL}",
      isAutoSyncEnabled = prefs.getBoolean("auto_sync_enabled", true),
      syncIntervalSeconds = prefs.getInt("sync_interval_seconds", 15),
      useHttps = prefs.getBoolean("use_https", false)
    )
  }

  fun updateConfig(newConfig: ServerConfig) {
    prefs.edit()
      .putString("server_host", newConfig.serverHost.trim())
      .putInt("server_port", newConfig.serverPort)
      .putString("api_key", newConfig.apiKey.trim())
      .putString("client_id", newConfig.clientId.trim())
      .putString("client_name", newConfig.clientName.trim())
      .putBoolean("auto_sync_enabled", newConfig.isAutoSyncEnabled)
      .putInt("sync_interval_seconds", newConfig.syncIntervalSeconds)
      .putBoolean("use_https", newConfig.useHttps)
      .apply()
    _config.value = newConfig
  }
}
