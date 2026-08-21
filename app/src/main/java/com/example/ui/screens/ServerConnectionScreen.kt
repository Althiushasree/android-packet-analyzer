package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.server.ConnectionStatus
import com.example.data.server.HealthResponse
import com.example.data.server.ServerConfig
import com.example.data.server.SyncLogEntry
import com.example.data.server.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ServerConnectionScreen(
  config: ServerConfig,
  connectionStatus: ConnectionStatus,
  serverHealth: HealthResponse?,
  lastPingLatencyMs: Long,
  lastConnectionTimestamp: Long,
  lastErrorMessage: String?,
  syncState: SyncState,
  syncedRecordsCount: Int,
  pendingRecordsCount: Int,
  failedRecordsCount: Int,
  lastSyncTimestamp: Long,
  lastSyncMessage: String,
  syncLogs: List<SyncLogEntry>,
  onUpdateConfig: (ServerConfig) -> Unit,
  onTestConnection: () -> Unit,
  onDisconnect: () -> Unit,
  onSyncNow: () -> Unit,
  modifier: Modifier = Modifier
) {
  var hostInput by remember(config.serverHost) { mutableStateOf(config.serverHost) }
  var portInput by remember(config.serverPort) { mutableStateOf(config.serverPort.toString()) }
  var apiKeyInput by remember(config.apiKey) { mutableStateOf(config.apiKey) }
  var clientIdInput by remember(config.clientId) { mutableStateOf(config.clientId) }
  var autoSyncEnabled by remember(config.isAutoSyncEnabled) { mutableStateOf(config.isAutoSyncEnabled) }
  var syncIntervalInput by remember(config.syncIntervalSeconds) { mutableStateOf(config.syncIntervalSeconds.toString()) }
  var showHelp by remember { mutableStateOf(false) }

  val timeFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }

  val isConnected = connectionStatus == ConnectionStatus.CONNECTED
  val isConnecting = connectionStatus == ConnectionStatus.CONNECTING || syncState == SyncState.SYNCING

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("server_connection_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. HEADER & LIVE CONNECTION STATUS BADGE
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Client-Server Central Gateway",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "FastAPI + PostgreSQL Synchronization",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              color = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> Color(0xFF10B981).copy(alpha = 0.15f)
                ConnectionStatus.CONNECTING -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
              },
              shape = RoundedCornerShape(20.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  color = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> Color(0xFF10B981)
                    ConnectionStatus.CONNECTING -> Color(0xFFF59E0B)
                    ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
                    ConnectionStatus.DISCONNECTED -> Color.Gray
                  },
                  shape = CircleShape,
                  modifier = Modifier.size(8.dp)
                ) {}
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> "CONNECTED"
                    ConnectionStatus.CONNECTING -> "CONNECTING..."
                    ConnectionStatus.ERROR -> "OFFLINE / ERROR"
                    ConnectionStatus.DISCONNECTED -> "DISCONNECTED"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> Color(0xFF10B981)
                    ConnectionStatus.CONNECTING -> Color(0xFFF59E0B)
                    ConnectionStatus.ERROR -> MaterialTheme.colorScheme.error
                    ConnectionStatus.DISCONNECTED -> Color.Gray
                  }
                )
              }
            }
          }

          if (serverHealth != null && isConnected) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              StatusChip(icon = Icons.Default.Speed, label = "Latency: ${lastPingLatencyMs}ms", color = Color(0xFF3B82F6))
              StatusChip(icon = Icons.Default.Storage, label = "DB: ${serverHealth.database}", color = Color(0xFF10B981))
              StatusChip(icon = Icons.Default.Computer, label = "Clients: ${serverHealth.clientsCount}", color = Color(0xFF8B5CF6))
              StatusChip(icon = Icons.Default.Sensors, label = "Sessions: ${serverHealth.sessionsCount}", color = Color(0xFF06B6D4))
            }
          }

          if (!lastErrorMessage.isNullOrBlank() && connectionStatus == ConnectionStatus.ERROR) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = lastErrorMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
              }
            }
          }
        }
      }
    }

    // 2. SYNCHRONIZATION METRICS CARDS
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SyncCounterCard(
          title = "Synced Records",
          count = syncedRecordsCount.toString(),
          icon = Icons.Default.CloudDone,
          color = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )
        SyncCounterCard(
          title = "Pending Room",
          count = pendingRecordsCount.toString(),
          icon = Icons.Default.Storage,
          color = Color(0xFFF59E0B),
          modifier = Modifier.weight(1f)
        )
        SyncCounterCard(
          title = "Failed / Retries",
          count = failedRecordsCount.toString(),
          icon = Icons.Default.CloudOff,
          color = if (failedRecordsCount > 0) MaterialTheme.colorScheme.error else Color.Gray,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // 3. SERVER CONFIGURATION & PRESETS
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Connection Configuration",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showHelp = !showHelp }) {
              Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Architecture Guide",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          AnimatedVisibility(visible = showHelp) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp)
            ) {
              Text(
                text = "Target Network Modes:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "• Same Device (Emulator): Host 10.0.2.2:8000 routes to your laptop's localhost:8000.\n" +
                  "• Same Device (Physical USB): Host 127.0.0.1:8000 via adb reverse tcp:8000 tcp:8000.\n" +
                  "• Two Laptops (LAN): Set Host to Server Laptop's Wi-Fi IP (e.g. 192.168.1.20:8000).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Quick Presets:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(4.dp))

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = hostInput == "10.0.2.2",
              onClick = {
                hostInput = "10.0.2.2"
                portInput = "8000"
              },
              label = { Text("Emulator (10.0.2.2)") },
              leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
            FilterChip(
              selected = hostInput == "127.0.0.1" || hostInput == "localhost",
              onClick = {
                hostInput = "127.0.0.1"
                portInput = "8000"
              },
              label = { Text("Localhost (127.0.0.1)") }
            )
            FilterChip(
              selected = hostInput.startsWith("192.168."),
              onClick = {
                if (!hostInput.startsWith("192.168.")) hostInput = "192.168.1.20"
              },
              label = { Text("LAN IP (192.168.x.x)") },
              leadingIcon = { Icon(Icons.Default.Lan, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = hostInput,
              onValueChange = { hostInput = it },
              label = { Text("Server Host / IP") },
              modifier = Modifier
                .weight(2.5f)
                .testTag("server_host_input"),
              singleLine = true,
              leadingIcon = { Icon(Icons.Default.Router, contentDescription = null) }
            )

            OutlinedTextField(
              value = portInput,
              onValueChange = { portInput = it },
              label = { Text("Port") },
              modifier = Modifier
                .weight(1f)
                .testTag("server_port_input"),
              singleLine = true
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("API Key / Bearer Token") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("server_api_key_input"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = clientIdInput,
            onValueChange = { clientIdInput = it },
            label = { Text("Client Identifier") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Computer, contentDescription = null) }
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Auto-Sync Loop", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text(text = "Periodically sync local Room database in background", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = autoSyncEnabled,
              onCheckedChange = { autoSyncEnabled = it }
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // ACTION BUTTONS
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                val newPort = portInput.toIntOrNull() ?: 8000
                val newCfg = config.copy(
                  serverHost = hostInput.trim(),
                  serverPort = newPort,
                  apiKey = apiKeyInput.trim(),
                  clientId = clientIdInput.trim(),
                  isAutoSyncEnabled = autoSyncEnabled
                )
                onUpdateConfig(newCfg)
                onTestConnection()
              },
              modifier = Modifier
                .weight(1f)
                .testTag("test_connection_button"),
              enabled = !isConnecting
            ) {
              if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
              } else {
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
              }
              Text("TEST")
            }

            if (isConnected) {
              OutlinedButton(
                onClick = onDisconnect,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                  .weight(1f)
                  .testTag("disconnect_button")
              ) {
                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DISCONNECT")
              }
            } else {
              Button(
                onClick = {
                  val newPort = portInput.toIntOrNull() ?: 8000
                  val newCfg = config.copy(
                    serverHost = hostInput.trim(),
                    serverPort = newPort,
                    apiKey = apiKeyInput.trim(),
                    clientId = clientIdInput.trim(),
                    isAutoSyncEnabled = autoSyncEnabled
                  )
                  onUpdateConfig(newCfg)
                  onTestConnection()
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("connect_button"),
                enabled = !isConnecting
              ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CONNECT")
              }
            }

            Button(
              onClick = onSyncNow,
              modifier = Modifier
                .weight(1f)
                .testTag("sync_now_button"),
              enabled = syncState != SyncState.SYNCING
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("SYNC")
            }
          }
        }
      }
    }

    // 4. SYNC ACTIVITY LOG
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Sync Activity & Pipeline Log",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (lastSyncTimestamp > 0) "Last: ${timeFormat.format(Date(lastSyncTimestamp))}" else "No syncs yet",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Status: $lastSyncMessage",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = when (syncState) {
              SyncState.SYNCED, SyncState.SUCCESS -> Color(0xFF10B981)
              SyncState.SYNCING -> Color(0xFF3B82F6)
              SyncState.PENDING -> Color(0xFFF59E0B)
              SyncState.FAILED -> MaterialTheme.colorScheme.error
              SyncState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
            }
          )

          Spacer(modifier = Modifier.height(10.dp))

          if (syncLogs.isEmpty()) {
            Text(
              text = "Logs will appear here as Room records are synchronized to PostgreSQL.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              syncLogs.take(8).forEach { log ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(
                      if (log.isSuccess) Color(0xFF10B981).copy(alpha = 0.08f) else MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                      RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                      imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                      contentDescription = null,
                      tint = if (log.isSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = log.message,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium
                    )
                  }
                  Text(
                    text = timeFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatusChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
  Surface(
    color = color.copy(alpha = 0.12f),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color
      )
    }
  }
}

@Composable
private fun SyncCounterCard(
  title: String,
  count: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = count,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }
  }
}
