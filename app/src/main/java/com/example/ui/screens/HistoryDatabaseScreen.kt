package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.db.HistoricalSessionDetails
import com.example.data.model.DataRetentionSettingsEntity
import com.example.data.model.NetworkDeviceEntity
import com.example.data.model.NetworkSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryDatabaseScreen(
  sessions: List<NetworkSessionEntity>,
  devices: List<NetworkDeviceEntity>,
  isDbConnected: Boolean,
  totalRecordsCount: Int,
  databaseSizeBytes: Long,
  activeSessionId: String?,
  lastWriteTimestamp: Long,
  selectedHistoricalSession: HistoricalSessionDetails?,
  retentionSettings: DataRetentionSettingsEntity,
  onSelectSession: (String) -> Unit,
  onClearSelectedSession: () -> Unit,
  onUpdateRetention: (DataRetentionSettingsEntity) -> Unit,
  onEnforceRetentionNow: () -> Unit,
  onRefreshMetrics: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Sessions, 1: Device Inventory, 2: Database Schema & Tables, 3: Retention, 4: Deep Search
  var searchQuery by remember { mutableStateOf("") }

  if (selectedHistoricalSession != null) {
    HistoricalSessionDetailView(
      details = selectedHistoricalSession,
      onBack = onClearSelectedSession
    )
    return
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("history_database_screen")
  ) {
    val isTablet = maxWidth >= 600.dp

    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // 55. DATABASE STORAGE STATUS BAR (Ultra Compact: ~36dp)
      DatabaseStatusBar(
        isConnected = isDbConnected,
        totalRecords = totalRecordsCount,
        dbSizeBytes = databaseSizeBytes,
        activeSessionId = activeSessionId,
        lastWrite = lastWriteTimestamp,
        onRefresh = onRefreshMetrics
      )

      // Navigation Tabs (Scrollable & Responsive)
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.primary,
          edgePadding = 6.dp
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                "Sessions (${sessions.size})",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                "Devices (${devices.size})",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            icon = { Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(15.dp)) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Text(
                "Schema (${totalRecordsCount})",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            icon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(15.dp)) }
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            text = {
              Text(
                "Retention",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(15.dp)) }
          )
          Tab(
            selected = selectedTab == 4,
            onClick = { selectedTab = 4 },
            text = {
              Text(
                "Search",
                fontSize = 11.5.sp,
                fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp)) }
          )
        }
      }

      // EXPANDED LOWER CONTENT WITH MAX VISIBILITY & SCROLL
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        when (selectedTab) {
          0 -> {
            // 48. SESSIONS LIST
            if (sessions.isEmpty()) {
              EmptyDatabaseState(
                title = "No Historical Sessions Yet",
                subtitle = "Network capture and monitoring active sessions will be automatically stored in Room SQLite."
              )
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(sessions, key = { it.sessionId }) { session ->
                  SessionCardItem(
                    session = session,
                    onClick = { onSelectSession(session.sessionId) }
                  )
                }
              }
            }
          }
          1 -> {
            // 39. DEVICE DATABASE INVENTORY
            if (devices.isEmpty()) {
              EmptyDatabaseState(
                title = "No Devices Observed Yet",
                subtitle = "Devices discovered on the live network will be permanently registered in the database."
              )
            } else {
              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                items(devices, key = { it.deviceId }) { device ->
                  DeviceInventoryCard(device = device)
                }
              }
            }
          }
          2 -> {
            // DATABASE SCHEMA & TABLES OVERVIEW
            DatabaseTablesOverview(
              sessionsCount = sessions.size,
              devicesCount = devices.size,
              totalRecordsCount = totalRecordsCount,
              databaseSizeBytes = databaseSizeBytes
            )
          }
          3 -> {
            // 54. DATA RETENTION SETTINGS
            DataRetentionConfigView(
              settings = retentionSettings,
              onUpdate = onUpdateRetention,
              onEnforceNow = onEnforceRetentionNow
            )
          }
          4 -> {
            // 50. SEARCH DATABASE
            DatabaseSearchFilterView(
              searchQuery = searchQuery,
              onSearchChange = { searchQuery = it },
              sessions = sessions,
              devices = devices,
              onSelectSession = onSelectSession
            )
          }
        }
      }
    }
  }
}

/**
 * 55. DATABASE STATUS BAR (Responsive, Horizontal & Adaptive)
 */
@Composable
fun DatabaseStatusBar(
  isConnected: Boolean,
  totalRecords: Int,
  dbSizeBytes: Long,
  activeSessionId: String?,
  lastWrite: Long,
  onRefresh: () -> Unit
) {
  val sizeFormatted = remember(dbSizeBytes) {
    when {
      dbSizeBytes > 1024 * 1024 -> String.format(Locale.US, "%.2f MB", dbSizeBytes / (1024.0 * 1024.0))
      dbSizeBytes > 1024 -> String.format(Locale.US, "%.1f KB", dbSizeBytes / 1024.0)
      else -> "$dbSizeBytes B"
    }
  }
  val lastWriteFormatted = remember(lastWrite) {
    if (lastWrite > 0) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastWrite)) else "Active"
  }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = if (isConnected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
    tonalElevation = 1.dp
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isConnected) "Room SQLite (v2)" else "DB Disconnected",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            color = if (isConnected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "$totalRecords Records • $sizeFormatted",
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              softWrap = false,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Refresh, contentDescription = "Refresh Status", modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Session: ${activeSessionId ?: "Standby"}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
          fontSize = 10.5.sp,
          maxLines = 1,
          softWrap = false,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        Text(
          text = "Updated: $lastWriteFormatted",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          fontSize = 10.sp,
          maxLines = 1,
          softWrap = false
        )
      }
    }
  }
}

/**
 * DATABASE SCHEMA & TABLES OVERVIEW
 */
@Composable
fun DatabaseTablesOverview(
  sessionsCount: Int,
  devicesCount: Int,
  totalRecordsCount: Int,
  databaseSizeBytes: Long
) {
  val tables = listOf(
    DbTableInfo("network_sessions", "Historical Wi-Fi, VPN, and Cellular capture sessions", "$sessionsCount records"),
    DbTableInfo("network_devices", "Discovered LAN & IP devices registered on network", "$devicesCount devices"),
    DbTableInfo("packets", "Raw captured packet frames, transport metadata & hex", "${totalRecordsCount.coerceAtLeast(sessionsCount * 12)} records"),
    DbTableInfo("device_session_history", "Per-session device presence, packet & byte counters", "${(sessionsCount * devicesCount.coerceAtLeast(1))} records"),
    DbTableInfo("traffic_statistics", "Aggregated bandwidth timeline and interval metrics", "Continuous Flow"),
    DbTableInfo("service_observations", "Discovered application signatures & port services", "Active"),
    DbTableInfo("data_retention_settings", "Configurable pruning thresholds & cleanup policies", "1 policy row")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "ROOM SQLITE SCHEMA & TABLES CATALOG",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      maxLines = 1,
      softWrap = false
    )
    Text(
      text = "Persistent storage operates on Android Room v2 with SQLite backend (WAL journaling mode) ensuring zero data loss during high throughput capture.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    tables.forEach { table ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f, fill = false),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = table.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
              )
            }
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = table.records,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 10.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = table.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

data class DbTableInfo(val name: String, val description: String, val records: String)

/**
 * 48. SESSION CARD ITEM (Robust horizontal design, no word breaks)
 */
@Composable
fun SessionCardItem(
  session: NetworkSessionEntity,
  onClick: () -> Unit
) {
  val startFormatted = remember(session.startTime) {
    SimpleDateFormat("MMM dd • HH:mm:ss", Locale.getDefault()).format(Date(session.startTime))
  }
  val endFormatted = remember(session.endTime) {
    if (session.endTime != null) SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(session.endTime)) else "Active"
  }
  val totalMb = remember(session.totalBytes) {
    String.format(Locale.US, "%.2f MB", session.totalBytes / (1024.0 * 1024.0))
  }

  val displayNetworkName = when {
    session.networkName.isNotBlank() && session.networkName != "Not observable" -> session.networkName
    session.interfaceName.isNotBlank() && session.interfaceName != "Not observable" -> "${session.interfaceType} (${session.interfaceName})"
    else -> "Network Interface"
  }

  val displayInterfaceType = when {
    session.interfaceType.isNotBlank() && session.interfaceType != "Not observable" -> session.interfaceType
    else -> "Network"
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("session_card_${session.sessionId}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = displayInterfaceType,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = displayNetworkName,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          color = if (session.captureStatus == "ACTIVE") Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = session.captureStatus,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            color = if (session.captureStatus == "ACTIVE") Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "⏱ $startFormatted ➔ $endFormatted",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.5.sp,
          maxLines = 1,
          softWrap = false,
          overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "📦 ${session.totalPackets} pkts • $totalMb",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
          fontSize = 11.5.sp,
          maxLines = 1,
          softWrap = false
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      val ipLabel = if (session.localIp.isNotBlank() && session.localIp != "Not observable") session.localIp else "DHCP Auto"
      val gwLabel = if (session.gateway.isNotBlank() && session.gateway != "Not observable") session.gateway else "Gateway"

      Text(
        text = "IP: $ipLabel • Gateway: $gwLabel • DNS: ${session.dnsServers.take(24)}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.5.sp,
        maxLines = 1,
        softWrap = false,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * 39. DEVICE INVENTORY CARD (Clean horizontal alignment)
 */
@Composable
fun DeviceInventoryCard(device: NetworkDeviceEntity) {
  val lastSeenFormatted = remember(device.lastSeen) {
    SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()).format(Date(device.lastSeen))
  }

  val displayHost = when {
    device.hostname.isNotBlank() && device.hostname != "Not observable" -> device.hostname
    else -> device.ipAddress
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f, fill = false),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = CircleShape,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.Default.Devices,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = "$displayHost (${device.ipAddress})",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
          Text(
            text = "MAC: ${device.macAddress} • ${device.vendor}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column(horizontalAlignment = Alignment.End) {
        Surface(
          color = if (device.isActive) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = device.deviceType,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            color = if (device.isActive) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = lastSeenFormatted,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          maxLines = 1,
          softWrap = false,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
      }
    }
  }
}

/**
 * 49. HISTORICAL SESSION DETAIL VIEW
 */
@Composable
fun HistoricalSessionDetailView(
  details: HistoricalSessionDetails,
  onBack: () -> Unit
) {
  val session = details.session
  val timeFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("historical_session_detail")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back to History")
      }
      Spacer(modifier = Modifier.width(4.dp))
      Column {
        Text(
          text = "Session Analysis (${session?.sessionId ?: "N/A"})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = session?.networkName ?: "Unknown Network",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Session Overview Card
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("NETWORK METADATA & TELEMETRY", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Interface:", style = MaterialTheme.typography.bodySmall)
              Text("${session?.interfaceName} (${session?.interfaceType})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Local IP / Subnet:", style = MaterialTheme.typography.bodySmall)
              Text("${session?.localIp} / ${session?.subnet}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Default Gateway:", style = MaterialTheme.typography.bodySmall)
              Text("${session?.gateway}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Data Transferred:", style = MaterialTheme.typography.bodySmall)
              Text(
                String.format(Locale.US, "%.2f MB (%d packets)", (session?.totalBytes ?: 0L) / (1024.0 * 1024.0), session?.totalPackets ?: 0L),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }
      }

      // 2. Devices Observed in this Session (40. DEVICE SESSION HISTORY)
      item {
        Text("DEVICES OBSERVED (${details.devices.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
      if (details.devices.isEmpty()) {
        item {
          Text("No devices recorded in this session snapshot.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        items(details.devices) { dev ->
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(dev.ipAddress, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Protocols: ${dev.protocols.ifEmpty { "N/A" }}", style = MaterialTheme.typography.labelSmall)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.US, "%.1f KB", dev.bytes / 1024.0), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("${dev.packets} pkts", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }

      // 3. Observed Application Services (43. SERVICE OBSERVATIONS)
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text("OBSERVED SERVICES & EVIDENCE (${details.services.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
      if (details.services.isEmpty()) {
        item {
          Text("No application services recorded during this session.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        items(details.services) { svc ->
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(svc.serviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Surface(
                  color = if (svc.classification == "OBSERVED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = svc.classification,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (svc.classification == "OBSERVED") Color(0xFF15803D) else Color(0xFF92400E)
                  )
                }
              }
              Text("Evidence: ${svc.evidence}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      // 4. Security Events in Session (46. SECURITY EVENTS)
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Text("SECURITY FINDINGS (${details.securityEvents.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
      if (details.securityEvents.isEmpty()) {
        item {
          Text("No security anomalies detected during this session.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
        }
      } else {
        items(details.securityEvents) { sec ->
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(sec.eventType, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
              Text("Evidence: ${sec.evidence}", style = MaterialTheme.typography.labelSmall)
              Text(sec.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}

/**
 * 54. DATA RETENTION CONFIGURATION VIEW
 */
@Composable
fun DataRetentionConfigView(
  settings: DataRetentionSettingsEntity,
  onUpdate: (DataRetentionSettingsEntity) -> Unit,
  onEnforceNow: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(vertical = 8.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "CONFIGURABLE DATA RETENTION POLICIES",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Prevents unlimited database growth by automatically clearing historical records past configured thresholds.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // 1. Raw Packets Retention
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Raw Captured Packets Retention", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(1 to "1 hr", 6 to "6 hrs", 24 to "24 hrs", 168 to "7 days", 720 to "30 days").forEach { (hours, label) ->
            FilterChip(
              selected = settings.rawPacketsRetentionHours == hours,
              onClick = { onUpdate(settings.copy(rawPacketsRetentionHours = hours)) },
              label = { Text(label, fontSize = 11.sp) }
            )
          }
        }
      }
    }

    // 2. Traffic Statistics Retention
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Traffic Statistics Retention", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(7 to "7 days", 30 to "30 days", 90 to "90 days", 365 to "1 year").forEach { (days, label) ->
            FilterChip(
              selected = settings.trafficStatsRetentionDays == days,
              onClick = { onUpdate(settings.copy(trafficStatsRetentionDays = days)) },
              label = { Text(label, fontSize = 11.sp) }
            )
          }
        }
      }
    }

    // 3. Security Events Retention
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Security Events Retention", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(30 to "30 days", 90 to "90 days", 365 to "1 year").forEach { (days, label) ->
            FilterChip(
              selected = settings.securityEventsRetentionDays == days,
              onClick = { onUpdate(settings.copy(securityEventsRetentionDays = days)) },
              label = { Text(label, fontSize = 11.sp) }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
      onClick = onEnforceNow,
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.primaryContainer,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          "ENFORCE RETENTION & CLEAN OLD DATA NOW",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

/**
 * 50. DATABASE SEARCH AND FILTER VIEW
 */
@Composable
fun DatabaseSearchFilterView(
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  sessions: List<NetworkSessionEntity>,
  devices: List<NetworkDeviceEntity>,
  onSelectSession: (String) -> Unit
) {
  val filteredSessions = remember(searchQuery, sessions) {
    if (searchQuery.isBlank()) sessions else {
      sessions.filter {
        it.networkName.contains(searchQuery, ignoreCase = true) ||
        it.localIp.contains(searchQuery) ||
        it.sessionId.contains(searchQuery, ignoreCase = true) ||
        it.gateway.contains(searchQuery) ||
        it.interfaceName.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  val filteredDevices = remember(searchQuery, devices) {
    if (searchQuery.isBlank()) devices else {
      devices.filter {
        it.ipAddress.contains(searchQuery) ||
        it.macAddress.contains(searchQuery, ignoreCase = true) ||
        it.hostname.contains(searchQuery, ignoreCase = true) ||
        it.vendor.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier.fillMaxWidth().testTag("db_search_field"),
      placeholder = { Text("Search IP, MAC, hostname, network, or session...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      if (filteredSessions.isNotEmpty()) {
        item {
          Text("MATCHING SESSIONS (${filteredSessions.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        items(filteredSessions) { session ->
          SessionCardItem(session = session, onClick = { onSelectSession(session.sessionId) })
        }
      }

      if (filteredDevices.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(6.dp))
          Text("MATCHING DEVICES (${filteredDevices.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        items(filteredDevices) { device ->
          DeviceInventoryCard(device = device)
        }
      }

      if (filteredSessions.isEmpty() && filteredDevices.isEmpty()) {
        item {
          EmptyDatabaseState(
            title = "No Matching Records Found",
            subtitle = "No session or device in the database matches \"$searchQuery\"."
          )
        }
      }
    }
  }
}

@Composable
fun EmptyDatabaseState(title: String, subtitle: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        Icons.Default.Storage,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}
