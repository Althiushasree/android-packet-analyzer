package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.intelligence.AnomalySeverity
import com.example.data.intelligence.ApplicationServiceAnalysis
import com.example.data.intelligence.CommunicationFlow
import com.example.data.intelligence.DefensiveSecurityAlert
import com.example.data.intelligence.DeviceType
import com.example.data.intelligence.IntelligenceStatus
import com.example.data.intelligence.ObservedNetworkDevice
import com.example.data.intelligence.RealNetworkInterfaceInfo
import com.example.data.model.AuthorizedNetworkScope
import com.example.data.model.NetworkSessionEntity
import com.example.util.SummaryReportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NetworkMonitoringScreen(
  networkScopes: List<AuthorizedNetworkScope>,
  selectedScope: AuthorizedNetworkScope,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>,
  services: List<ApplicationServiceAnalysis>,
  alerts: List<DefensiveSecurityAlert>,
  networkInfo: RealNetworkInterfaceInfo? = null,
  historicalSessions: List<NetworkSessionEntity> = emptyList(),
  onSelectNetworkScope: (String) -> Unit,
  onExportReport: (format: String, target: String, timeRange: String) -> String,
  onRefreshDiscovery: () -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var scopeDropdownExpanded by remember { mutableStateOf(false) }

  var selectedDeviceForModal by remember { mutableStateOf<ObservedNetworkDevice?>(null) }
  var selectedFlowForModal by remember { mutableStateOf<CommunicationFlow?>(null) }

  val tabs = listOf(
    "Real Network Analysis" to Icons.Default.Info,
    "Overview" to Icons.Default.Public,
    "Connected Devices" to Icons.Default.Devices,
    "Traffic Flows" to Icons.Default.AltRoute,
    "Applications & Services" to Icons.Default.Hub,
    "Communication Map" to Icons.Default.DeviceHub,
    "Protocols" to Icons.Default.BarChart,
    "Alerts" to Icons.Default.Security,
    "Network History" to Icons.Default.History,
    "Export" to Icons.Default.Download
  )

  var showVisibilityInfoDialog by remember { mutableStateOf(false) }

  if (showVisibilityInfoDialog) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showVisibilityInfoDialog = false },
      icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
      title = { Text("Network Visibility Mode", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          if (selectedScope.networkVisibilityMode == "COLLECTOR_CONNECTED") {
            "Authorized Remote Collector Active: Streaming gateway & AP sensor telemetry."
          } else {
            "Local Device Visibility: Android endpoint interface cannot passively observe other Wi-Fi clients without an authorized TAP/SPAN/Gateway sensor."
          }
        )
      },
      confirmButton = {
        TextButton(onClick = { showVisibilityInfoDialog = false }) { Text("Got it") }
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("network_monitoring_screen")
  ) {
    // 1. ULTRA-COMPACT TOP HEADER & REAL NETWORK SCOPE SELECTOR (~38dp)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 4.dp),
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      tonalElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFF16A34A))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF16A34A),
            fontSize = 10.sp,
            maxLines = 1,
            softWrap = false
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = selectedScope.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Network Scope Dropdown Button
          Box {
            OutlinedButton(
              onClick = { scopeDropdownExpanded = true },
              shape = RoundedCornerShape(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Switch",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
              )
            }

            DropdownMenu(
              expanded = scopeDropdownExpanded,
              onDismissRequest = { scopeDropdownExpanded = false }
            ) {
              Text(
                text = "SELECT ACTIVE / RECORDED NETWORK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
              HorizontalDivider()
              networkScopes.forEach { scopeItem ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(scopeItem.name, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                      Text(
                        "${scopeItem.subnet} • ${scopeItem.monitoringInterface}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                      )
                    }
                  },
                  onClick = {
                    onSelectNetworkScope(scopeItem.id)
                    scopeDropdownExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = { showVisibilityInfoDialog = true },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Visibility Info",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp)
            )
          }

          IconButton(
            onClick = onRefreshDiscovery,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Refresh Discovery",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }
    }

    // 2. SUB-PAGES NAVIGATION (Compact tab bar)
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 8.dp
    ) {
      tabs.forEachIndexed { index, (title, icon) ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          modifier = Modifier.testTag("net_mon_tab_$index"),
          icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(16.dp)) },
          text = {
            Text(
              text = title,
              fontSize = 11.sp,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1,
              softWrap = false
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. EXPANDED SUB-PAGE CONTENT (Full height weight & multi-directional scroll)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    ) {
      when (selectedTab) {
        0 -> RealNetworkAnalysisTab(
          scope = selectedScope,
          networkInfo = networkInfo,
          devices = devices,
          flows = flows,
          onRefresh = onRefreshDiscovery
        )
        1 -> MonitoringOverviewTab(
          scope = selectedScope,
          networkInfo = networkInfo,
          devices = devices,
          flows = flows,
          services = services,
          alerts = alerts,
          onInspectDevice = { selectedDeviceForModal = it },
          onNavigateToTab = { selectedTab = it },
          onRefresh = onRefreshDiscovery
        )
        2 -> ConnectedDevicesTab(
          scope = selectedScope,
          devices = devices,
          onDeviceClick = { selectedDeviceForModal = it },
          onExport = {
            val csv = onExportReport("CSV", "Connected Devices", "All Time")
            SummaryReportUtils.shareTextReport(context, csv, "Export Connected Devices CSV")
          }
        )
        3 -> TrafficFlowsTab(
          scope = selectedScope,
          flows = flows,
          onFlowClick = { selectedFlowForModal = it }
        )
        4 -> ApplicationsServicesTab(
          services = services,
          onInspectService = {}
        )
        5 -> CommunicationMapTab(
          scope = selectedScope,
          devices = devices,
          flows = flows,
          services = services,
          onSelectDevice = { selectedDeviceForModal = it }
        )
        6 -> ProtocolsTab(
          devices = devices,
          flows = flows
        )
        7 -> SecurityAlertsTab(
          alerts = alerts,
          onInspectDevice = { ip ->
            devices.firstOrNull { it.ipAddress == ip }?.let { selectedDeviceForModal = it }
          }
        )
        8 -> NetworkHistoryTab(
          scope = selectedScope,
          historicalSessions = historicalSessions
        )
        9 -> ExportTab(
          scope = selectedScope,
          onGenerateExport = onExportReport
        )
      }
    }
  }

  // 4. DEVICE DETAILS MODAL
  selectedDeviceForModal?.let { device ->
    DeviceDetailsModal(
      device = device,
      scope = selectedScope,
      allFlows = flows.filter { it.sourceDeviceIp == device.ipAddress || it.destinationAddress == device.ipAddress },
      allServices = services.filter { it.deviceIp.contains(device.ipAddress) || it.deviceIp.contains("devices") },
      onDismiss = { selectedDeviceForModal = null }
    )
  }

  // 5. FLOW DETAILS MODAL
  selectedFlowForModal?.let { flow ->
    FlowDetailsModal(
      flow = flow,
      scope = selectedScope,
      onDismiss = { selectedFlowForModal = null }
    )
  }
}

// -----------------------------------------------------------------------------------------
// 0. REAL NETWORK ANALYSIS TAB (Requirement Section 14)
// -----------------------------------------------------------------------------------------
@Composable
private fun RealNetworkAnalysisTab(
  scope: AuthorizedNetworkScope,
  networkInfo: RealNetworkInterfaceInfo?,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>,
  onRefresh: () -> Unit
) {
  val info = networkInfo ?: RealNetworkInterfaceInfo()
  val isConnected = info.isConnected && info.localIpv4 != "Not observable"
  val actualSsid = if (scope.ssid.isNotBlank() && !scope.ssid.contains("Not observable")) scope.ssid else if (info.ssid.isNotBlank() && !info.ssid.contains("Not observable")) info.ssid else "Not observable on current network"
  val actualIface = if (info.interfaceName != "Not observable") "${info.interfaceName} (${info.interfaceType})" else scope.monitoringInterface
  val actualLocalIp = if (info.localIpv4 != "Not observable") info.localIpv4 else "REAL NETWORK DATA UNAVAILABLE"
  val actualGateway = if (info.defaultGateway != "Not observable") info.defaultGateway else if (scope.gatewayIp.isNotBlank()) scope.gatewayIp else "REAL NETWORK DATA UNAVAILABLE"
  val actualSubnet = if (info.subnetMask != "Not observable") "${info.localIpv4.substringBeforeLast(".")}.0 / ${info.subnetMask}" else scope.subnet
  val actualCidr = if (info.subnetPrefixLength > 0) "/${info.subnetPrefixLength}" else scope.cidr
  val actualDns = if (info.dnsServers.isNotEmpty() && !info.dnsServers.contains("Not observable on current network")) info.dnsServers.joinToString(", ") else scope.dnsServers.joinToString(", ")
  val actualDhcp = if (info.dhcpServer != "Not observable") info.dhcpServer else scope.dhcpServer

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Section: NETWORK INFORMATION
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "NETWORK INFORMATION",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        NetworkInfoRow(label = "SSID", value = actualSsid, highlight = true)
        NetworkInfoRow(label = "INTERFACE", value = actualIface)
        NetworkInfoRow(label = "LOCAL IP", value = actualLocalIp, monospace = true)
        NetworkInfoRow(label = "GATEWAY", value = actualGateway, monospace = true)
        NetworkInfoRow(label = "SUBNET", value = actualSubnet, monospace = true)
        NetworkInfoRow(label = "CIDR", value = actualCidr, monospace = true)
        NetworkInfoRow(label = "DNS", value = actualDns, monospace = true)
        NetworkInfoRow(label = "DHCP SERVER", value = actualDhcp, monospace = true)
        NetworkInfoRow(
          label = "STATUS",
          value = if (isConnected) "Connected" else "Disconnected / Interface Offline",
          badgeColor = if (isConnected) Color(0xFF16A34A) else Color(0xFFDC2626)
        )
        NetworkInfoRow(
          label = "CONNECTION DURATION",
          value = "${info.connectionDurationSeconds} seconds"
        )
      }
    }

    // 2. Section: NETWORK VISIBILITY
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "NETWORK VISIBILITY",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(10.dp))

        NetworkInfoRow(
          label = "Local device visibility",
          value = if (isConnected) "Available" else "Unavailable",
          badgeColor = if (isConnected) Color(0xFF16A34A) else Color(0xFFDC2626)
        )

        val isCollector = scope.networkVisibilityMode == "COLLECTOR_CONNECTED"
        NetworkInfoRow(
          label = "Network-wide visibility",
          value = if (isCollector) "Available (Authorized Collector)" else "Unavailable (Android Endpoint)",
          badgeColor = if (isCollector) Color(0xFF16A34A) else Color(0xFFEAB308)
        )

        NetworkInfoRow(
          label = "Monitoring source",
          value = scope.monitoringSource
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "TECHNICAL DISCLOSURE",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "This device operates directly on the Android operating system. An Android endpoint cannot passively capture all Wi-Fi packets broadcast by other devices on the same access point. Local device traffic is captured via on-device interface / VPN service. For network-wide client visibility, configure an authorized remote collector on the gateway, router, or SPAN mirror port.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 14.sp
            )
          }
        }
      }
    }

    // 3. Observed Real Endpoint Telemetry
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "OBSERVED TELEMETRY SUMMARY",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          KpiMetricCard(
            title = "Observed Devs",
            value = "${devices.size}",
            subtitle = if (devices.isEmpty()) "0 observed" else "${devices.count { it.isActive }} online",
            icon = Icons.Default.Devices,
            color = Color(0xFF2563EB),
            modifier = Modifier.weight(1f)
          )
          KpiMetricCard(
            title = "Observed Flows",
            value = "${flows.size}",
            subtitle = if (flows.isEmpty()) "No packets" else "Active sockets",
            icon = Icons.Default.AltRoute,
            color = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          KpiMetricCard(
            title = "Total RX (Down)",
            value = formatBytes(info.rxBytes),
            subtitle = "${info.rxPackets} pkts",
            icon = Icons.Default.CloudDownload,
            color = Color(0xFF16A34A),
            modifier = Modifier.weight(1f)
          )
          KpiMetricCard(
            title = "Total TX (Up)",
            value = formatBytes(info.txBytes),
            subtitle = "${info.txPackets} pkts",
            icon = Icons.Default.ArrowUpward,
            color = Color(0xFF0D9488),
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun NetworkInfoRow(
  label: String,
  value: String,
  monospace: Boolean = false,
  highlight: Boolean = false,
  badgeColor: Color? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
    )

    if (badgeColor != null) {
      Surface(
        color = badgeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = badgeColor,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    } else {
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
        fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End,
        modifier = Modifier.weight(1f, fill = false).padding(start = 12.dp)
      )
    }
  }
}

// -----------------------------------------------------------------------------------------
// 1. OVERVIEW TAB
// -----------------------------------------------------------------------------------------
@Composable
private fun MonitoringOverviewTab(
  scope: AuthorizedNetworkScope,
  networkInfo: RealNetworkInterfaceInfo?,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>,
  services: List<ApplicationServiceAnalysis>,
  alerts: List<DefensiveSecurityAlert>,
  onInspectDevice: (ObservedNetworkDevice) -> Unit,
  onNavigateToTab: (Int) -> Unit,
  onRefresh: () -> Unit
) {
  val info = networkInfo ?: RealNetworkInterfaceInfo()
  var bandwidthFilterTime by remember { mutableStateOf("All") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // A. Network Discovery Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = scope.name.uppercase(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "SSID: ${scope.ssid} • Subnet: ${scope.subnet} (${scope.cidr})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
          }
          IconButton(onClick = onRefresh) {
            Icon(
              Icons.Default.Refresh,
              contentDescription = "Refresh Discovery",
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          InfoChip(label = "Gateway", value = scope.gatewayIp, modifier = Modifier.weight(1f))
          InfoChip(label = "Active Devs", value = "${devices.count { it.isActive }}", modifier = Modifier.weight(1f))
          InfoChip(label = "Total Devs", value = "${devices.size}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          InfoChip(label = "DHCP Server", value = scope.dhcpServer, modifier = Modifier.weight(1.5f))
          InfoChip(label = "DNS", value = scope.dnsServers.joinToString(", "), modifier = Modifier.weight(1.5f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          InfoChip(label = "Monitoring Source", value = scope.monitoringSource, modifier = Modifier.weight(2f))
          InfoChip(label = "Sensor Interface", value = scope.monitoringInterface, modifier = Modifier.weight(1.5f))
        }
      }
    }

    // B. Top KPI Stat Grid
    val totalTraffic = if (devices.isNotEmpty()) devices.sumOf { it.totalBytes } else (info.rxBytes + info.txBytes)
    val totalUpload = if (devices.isNotEmpty()) devices.sumOf { it.uploadBytes } else info.txBytes
    val totalDownload = if (devices.isNotEmpty()) devices.sumOf { it.downloadBytes } else info.rxBytes

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KpiMetricCard(
        title = "Connected Devs",
        value = "${devices.size}",
        subtitle = if (devices.isEmpty()) "0 observed" else "${devices.count { it.isActive }} Online",
        icon = Icons.Default.Devices,
        color = Color(0xFF2563EB),
        modifier = Modifier.weight(1f)
      )
      KpiMetricCard(
        title = "Active Flows",
        value = "${flows.size}",
        subtitle = if (flows.isEmpty()) "0 packets observed" else "Live Sockets",
        icon = Icons.Default.AltRoute,
        color = Color(0xFF7C3AED),
        modifier = Modifier.weight(1f)
      )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KpiMetricCard(
        title = "Total Traffic",
        value = formatBytes(totalTraffic),
        subtitle = "Actual Observed",
        icon = Icons.Default.Analytics,
        color = Color(0xFF0D9488),
        modifier = Modifier.weight(1f)
      )
      KpiMetricCard(
        title = "Down / Up",
        value = "${formatBytes(totalDownload)} ↓",
        subtitle = "${formatBytes(totalUpload)} ↑",
        icon = Icons.Default.CloudDownload,
        color = Color(0xFF16A34A),
        modifier = Modifier.weight(1f)
      )
    }

    // C. Top Bandwidth Users (Requirement Section 7)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Top Bandwidth Consumers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Observed upload/download utilization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          TextButton(onClick = { onNavigateToTab(2) }) {
            Text("View All (${devices.size})", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val topDevices = devices.sortedByDescending { it.totalBytes }.take(4)
        if (topDevices.isEmpty()) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                "No other devices observed from this interface.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                "Network-wide device discovery requires authorized gateway/AP telemetry or a network monitoring sensor.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
              )
            }
          }
        } else {
          val maxBytes = topDevices.first().totalBytes.coerceAtLeast(1L)
          topDevices.forEachIndexed { index, dev ->
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onInspectDevice(dev) }
                .padding(vertical = 6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "${dev.hostname.ifBlank { dev.ipAddress }} (${dev.ipAddress})",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "${dev.vendor} • ${dev.estimatedDeviceType}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      fontSize = 10.sp
                    )
                  }
                }

                Text(
                  text = formatBytes(dev.totalBytes),
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(4.dp))
              val progress = (dev.totalBytes.toFloat() / maxBytes.toFloat()).coerceIn(0.05f, 1f)
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp)),
                color = when (index) {
                  0 -> Color(0xFFEF4444)
                  1 -> Color(0xFFF97316)
                  2 -> Color(0xFFEAB308)
                  else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }
      }
    }

    // D. Application & Service Telemetry Summary
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Observed Applications & Services", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          TextButton(onClick = { onNavigateToTab(4) }) {
            Text("All Services (${services.size})", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (services.isEmpty()) {
          Text("No active services identified on ${scope.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          services.take(4).forEach { svc ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = if (svc.status == IntelligenceStatus.OBSERVED) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = svc.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (svc.status == IntelligenceStatus.OBSERVED) Color(0xFF16A34A) else Color(0xFF64748B),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(svc.serviceName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                  Text(svc.evidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
              }
              Text(formatBytes(svc.trafficBytes), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // E. Recent Security / Anomaly Alerts
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Passive Anomaly & Security Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          TextButton(onClick = { onNavigateToTab(7) }) {
            Text("All Alerts (${alerts.size})", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (alerts.isEmpty()) {
          Text("No security anomalies detected on ${scope.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          alerts.take(2).forEach { alert ->
            SecurityAlertMiniCard(alert)
            Spacer(modifier = Modifier.height(6.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

// -----------------------------------------------------------------------------------------
// 2. CONNECTED DEVICES TAB (Requirement Section 2 & 5)
// -----------------------------------------------------------------------------------------
@Composable
private fun ConnectedDevicesTab(
  scope: AuthorizedNetworkScope,
  devices: List<ObservedNetworkDevice>,
  onDeviceClick: (ObservedNetworkDevice) -> Unit,
  onExport: () -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") }
  var sortBy by remember { mutableStateOf("Total Traffic") }

  val filtered = devices.filter { dev ->
    val matchesSearch = dev.ipAddress.contains(searchQuery, ignoreCase = true) ||
      dev.hostname.contains(searchQuery, ignoreCase = true) ||
      dev.vendor.contains(searchQuery, ignoreCase = true) ||
      dev.macAddress.contains(searchQuery, ignoreCase = true)

    val matchesFilter = when (selectedFilter) {
      "ONLINE" -> dev.isActive
      "OFFLINE" -> !dev.isActive
      "LAPTOPS" -> dev.estimatedDeviceType == DeviceType.LAPTOP
      "SMARTPHONES" -> dev.estimatedDeviceType == DeviceType.SMARTPHONE
      "SERVERS" -> dev.estimatedDeviceType == DeviceType.SERVER || dev.estimatedDeviceType == DeviceType.GATEWAY
      else -> true
    }
    matchesSearch && matchesFilter
  }.let { list ->
    when (sortBy) {
      "Upload" -> list.sortedByDescending { it.uploadBytes }
      "Download" -> list.sortedByDescending { it.downloadBytes }
      "Connections" -> list.sortedByDescending { it.activeConnectionsCount }
      "Last Seen" -> list.sortedByDescending { it.lastSeenTimestamp }
      "IP Address" -> list.sortedBy { it.ipAddress }
      else -> list.sortedByDescending { it.totalBytes }
    }
  }

  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    // Search Bar & Export
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search IP, MAC, hostname, vendor...", fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
      )

      IconButton(onClick = onExport) {
        Icon(Icons.Default.Download, contentDescription = "Export Devices", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      val filters = listOf("ALL", "ONLINE", "OFFLINE", "LAPTOPS", "SMARTPHONES", "SERVERS")
      items(filters) { f ->
        FilterChip(
          selected = selectedFilter == f,
          onClick = { selectedFilter = f },
          label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Device List Header
    Text(
      text = "${filtered.size} Discovered Devices on ${scope.name}",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(4.dp))

    if (filtered.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No devices observed.",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Network-wide device discovery requires authorized gateway/AP telemetry or a network monitoring sensor. Other client traffic is not visible from this interface without an authorized sensor.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filtered) { dev ->
          DeviceItemCard(
            device = dev,
            onClick = { onDeviceClick(dev) }
          )
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun DeviceItemCard(
  device: ObservedNetworkDevice,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (device.isActive) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = getDeviceIcon(device.estimatedDeviceType),
                contentDescription = null,
                tint = if (device.isActive) Color(0xFF16A34A) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              if (device.isGateway) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    "GATEWAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
              if (device.isLocalHost) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = Color(0xFF2563EB).copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    "THIS DEVICE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = "${device.hostname} • ${device.vendor}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatBytes(device.totalBytes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Surface(
            color = if (device.isActive) Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = if (device.isActive) "ONLINE" else "OFFLINE",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (device.isActive) Color(0xFF16A34A) else Color(0xFF64748B),
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          "MAC: ${device.macAddress}",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          "↓ ${formatBytes(device.downloadBytes)}  ↑ ${formatBytes(device.uploadBytes)}  •  ${device.activeConnectionsCount} conns",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// 3. TRAFFIC FLOWS TAB (Requirement Section 3 & 6)
// -----------------------------------------------------------------------------------------
@Composable
private fun TrafficFlowsTab(
  scope: AuthorizedNetworkScope,
  flows: List<CommunicationFlow>,
  onFlowClick: (CommunicationFlow) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedProtocolFilter by remember { mutableStateOf("ALL") }

  val filtered = flows.filter { f ->
    val matchesSearch = f.sourceDeviceIp.contains(searchQuery, ignoreCase = true) ||
      f.destinationAddress.contains(searchQuery, ignoreCase = true) ||
      f.destinationDomain.contains(searchQuery, ignoreCase = true) ||
      f.protocol.contains(searchQuery, ignoreCase = true) ||
      f.port.toString().contains(searchQuery)

    val matchesProtocol = when (selectedProtocolFilter) {
      "HTTPS/TLS" -> f.protocol.contains("TLS") || f.protocol.contains("HTTPS")
      "TCP" -> f.protocol.contains("TCP")
      "UDP/QUIC" -> f.protocol.contains("UDP") || f.protocol.contains("QUIC")
      "DNS" -> f.protocol.contains("DNS")
      else -> true
    }
    matchesSearch && matchesProtocol
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Filter flows by IP, domain, port, protocol...", fontSize = 12.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      val protoFilters = listOf("ALL", "HTTPS/TLS", "TCP", "UDP/QUIC", "DNS")
      items(protoFilters) { pf ->
        FilterChip(
          selected = selectedProtocolFilter == pf,
          onClick = { selectedProtocolFilter = pf },
          label = { Text(pf, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
      shape = RoundedCornerShape(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Observed Flow Metadata Only • No payload, password, or private chat contents captured.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    if (filtered.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No packets observed.",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "0 active communication flows observed on this interface. Traffic will appear in real-time as packets are transmitted or received.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filtered) { flow ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onFlowClick(flow) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = flow.sourceDeviceIp,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                      .size(14.dp)
                      .padding(horizontal = 2.dp),
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "${flow.destinationAddress}:${flow.port}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = flow.protocol,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              if (flow.destinationDomain.isNotBlank()) {
                Text(
                  text = "Domain: ${flow.destinationDomain}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  "Packets: ${flow.packetCount}  •  Bytes: ${formatBytes(flow.totalBytes)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "Last Seen: ${flow.lastSeenFormatted}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// 4. APPLICATIONS & SERVICES TAB (Requirement Section 4 & 7)
// -----------------------------------------------------------------------------------------
@Composable
private fun ApplicationsServicesTab(
  services: List<ApplicationServiceAnalysis>,
  onInspectService: (ApplicationServiceAnalysis) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var filterClassification by remember { mutableStateOf("ALL") }

  val filtered = services.filter { s ->
    val matchesSearch = s.serviceName.contains(searchQuery, ignoreCase = true) ||
      s.deviceIp.contains(searchQuery, ignoreCase = true) ||
      s.evidence.contains(searchQuery, ignoreCase = true)

    val matchesClass = when (filterClassification) {
      "OBSERVED" -> s.status == IntelligenceStatus.OBSERVED
      "INFERRED" -> s.status == IntelligenceStatus.INFERRED
      "UNKNOWN" -> s.status == IntelligenceStatus.UNKNOWN
      else -> true
    }
    matchesSearch && matchesClass
  }

  Column(modifier = Modifier.fillMaxSize()) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search services, domains, ports...", fontSize = 12.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      val classifications = listOf("ALL", "OBSERVED", "INFERRED", "UNKNOWN")
      items(classifications) { cf ->
        FilterChip(
          selected = filterClassification == cf,
          onClick = { filterClassification = cf },
          label = { Text(cf, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (filtered.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No active services observed.",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Applications and services are classified strictly based on observed DNS queries, port numbers, and unencrypted metadata. If traffic is encrypted without identifiable metadata, it is reported honestly as Encrypted / Unknown Service.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filtered) { service ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = when (service.status) {
                      IntelligenceStatus.OBSERVED -> Color(0xFF16A34A).copy(alpha = 0.15f)
                      IntelligenceStatus.INFERRED -> Color(0xFF2563EB).copy(alpha = 0.15f)
                      else -> Color(0xFF64748B).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = service.status.name,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = when (service.status) {
                        IntelligenceStatus.OBSERVED -> Color(0xFF16A34A)
                        IntelligenceStatus.INFERRED -> Color(0xFF2563EB)
                        else -> Color(0xFF64748B)
                      },
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(service.serviceName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Text(formatBytes(service.trafficBytes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "Target Device: ${service.deviceIp}  •  Ports: ${service.portsUsed.joinToString(", ")} (${service.protocol})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(4.dp))

              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text("Detection Evidence:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                  Text(service.evidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = service.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
              )
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// 5. COMMUNICATION MAP TAB (Requirement Section 5 & 11)
// -----------------------------------------------------------------------------------------
@Composable
private fun CommunicationMapTab(
  scope: AuthorizedNetworkScope,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>,
  services: List<ApplicationServiceAnalysis>,
  onSelectDevice: (ObservedNetworkDevice) -> Unit
) {
  var zoomScale by remember { mutableFloatStateOf(1.0f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }
  var selectedNodeId by remember { mutableStateOf<String?>(null) }
  var filterProtocol by remember { mutableStateOf("ALL") }

  Column(modifier = Modifier.fillMaxSize()) {
    // Toolbar controls
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { zoomScale = (zoomScale * 1.2f).coerceAtMost(3.0f) }) {
            Icon(Icons.Default.Add, contentDescription = "Zoom In")
          }
          IconButton(onClick = { zoomScale = (zoomScale / 1.2f).coerceAtLeast(0.5f) }) {
            Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
          }
          IconButton(onClick = {
            zoomScale = 1.0f
            panOffset = Offset.Zero
          }) {
            Icon(Icons.Default.RestartAlt, contentDescription = "Reset View")
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf("ALL", "HTTPS", "TCP", "UDP").forEach { p ->
            FilterChip(
              selected = filterProtocol == p,
              onClick = { filterProtocol = p },
              label = { Text(p, fontSize = 10.sp) },
              shape = RoundedCornerShape(6.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Interactive Graph Canvas
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
      shape = RoundedCornerShape(14.dp)
    ) {
      if (devices.isEmpty() && flows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.DeviceHub, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("0 Active Flows Observed", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Real communication graph renders live connection edges between observed endpoints.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF94A3B8),
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
              detectTransformGestures { _, pan, zoom, _ ->
                zoomScale = (zoomScale * zoom).coerceIn(0.5f, 3.0f)
                panOffset += pan
              }
            }
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f + panOffset.x
            val centerY = size.height / 2f + panOffset.y

            // Gateway Central Node
            val gatewayRadius = 35f * zoomScale
            drawCircle(
              color = Color(0xFF2563EB),
              radius = gatewayRadius,
              center = Offset(centerX, centerY)
            )

            // Placement of observed devices around gateway
            val count = devices.size.coerceAtLeast(1)
            val orbitRadius = 160f * zoomScale

            devices.forEachIndexed { index, dev ->
              val angle = (2 * Math.PI / count) * index
              val nodeX = centerX + (orbitRadius * cos(angle)).toFloat()
              val nodeY = centerY + (orbitRadius * sin(angle)).toFloat()

              // Edge from gateway to device
              drawLine(
                color = if (dev.isActive) Color(0xFF16A34A).copy(alpha = 0.7f) else Color(0xFF64748B).copy(alpha = 0.4f),
                start = Offset(centerX, centerY),
                end = Offset(nodeX, nodeY),
                strokeWidth = (if (dev.isActive) 3f else 1.5f) * zoomScale
              )

              // Device node
              val devRadius = (if (dev.id == selectedNodeId) 24f else 18f) * zoomScale
              drawCircle(
                color = if (dev.isActive) Color(0xFF10B981) else Color(0xFF64748B),
                radius = devRadius,
                center = Offset(nodeX, nodeY)
              )
            }

            // Real External service nodes if identified
            val realServices = services.map { it.serviceName }.distinct().take(6)
            if (realServices.isNotEmpty()) {
              val outerRadius = 260f * zoomScale
              realServices.forEachIndexed { sIdx, sName ->
                val sAngle = (2 * Math.PI / realServices.size) * sIdx + 0.3
                val sX = centerX + (outerRadius * cos(sAngle)).toFloat()
                val sY = centerY + (outerRadius * sin(sAngle)).toFloat()

                // Outer edge to gateway
                drawLine(
                  color = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                  start = Offset(centerX, centerY),
                  end = Offset(sX, sY),
                  strokeWidth = 2f * zoomScale,
                  pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                drawCircle(
                  color = Color(0xFF8B5CF6),
                  radius = 16f * zoomScale,
                  center = Offset(sX, sY)
                )
              }
            }
          }

          // Legend
          Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(10.dp)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Text("Topology Legend", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gateway (${scope.gatewayIp})", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Observed Devices", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
              }
            }
          }
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// 6. PROTOCOLS TAB (Requirement Section 6)
// -----------------------------------------------------------------------------------------
@Composable
private fun ProtocolsTab(
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>
) {
  val totalFlows = flows.size.coerceAtLeast(1)
  val totalFlowBytes = flows.sumOf { it.totalBytes }.coerceAtLeast(1L)

  val httpsFlows = flows.filter { it.protocol.contains("TLS") || it.protocol.contains("HTTPS") || it.port == 443 }
  val tcpFlows = flows.filter { it.protocol.contains("TCP") && it.port != 443 }
  val udpFlows = flows.filter { it.protocol.contains("UDP") || it.protocol.contains("QUIC") }
  val dnsFlows = flows.filter { it.protocol.contains("DNS") || it.port == 53 }

  val protocolBreakdown = listOf(
    ProtocolStat("HTTPS / TLS (443)", httpsFlows.sumOf { it.totalBytes }, (httpsFlows.sumOf { it.totalBytes }.toFloat() / totalFlowBytes.toFloat()) * 100f, Color(0xFF16A34A)),
    ProtocolStat("TCP Stream", tcpFlows.sumOf { it.totalBytes }, (tcpFlows.sumOf { it.totalBytes }.toFloat() / totalFlowBytes.toFloat()) * 100f, Color(0xFF2563EB)),
    ProtocolStat("UDP / QUIC", udpFlows.sumOf { it.totalBytes }, (udpFlows.sumOf { it.totalBytes }.toFloat() / totalFlowBytes.toFloat()) * 100f, Color(0xFF7C3AED)),
    ProtocolStat("DNS Query (53)", dnsFlows.sumOf { it.totalBytes }, (dnsFlows.sumOf { it.totalBytes }.toFloat() / totalFlowBytes.toFloat()) * 100f, Color(0xFFF59E0B))
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("Observed Transport & Port Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Text("Measured protocol breakdown across all active flows", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(4.dp))
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Traffic Share by Protocol", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))

          protocolBreakdown.forEach { p ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(p.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${String.format(Locale.US, "%.1f", p.percentage)}% • ${formatBytes(p.bytes)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = p.color)
              }
              Spacer(modifier = Modifier.height(3.dp))
              LinearProgressIndicator(
                progress = { (p.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = p.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

private data class ProtocolStat(
  val name: String,
  val bytes: Long,
  val percentage: Float,
  val color: Color
)

// -----------------------------------------------------------------------------------------
// 7. ALERTS TAB (Requirement Section 9)
// -----------------------------------------------------------------------------------------
@Composable
private fun SecurityAlertsTab(
  alerts: List<DefensiveSecurityAlert>,
  onInspectDevice: (String) -> Unit
) {
  var selectedSeverity by remember { mutableStateOf("ALL") }

  val filtered = alerts.filter { a ->
    if (selectedSeverity == "ALL") true else a.severity.name == selectedSeverity
  }

  Column(modifier = Modifier.fillMaxSize()) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
      val severities = listOf("ALL", "HIGH", "MEDIUM", "LOW")
      items(severities) { s ->
        FilterChip(
          selected = selectedSeverity == s,
          onClick = { selectedSeverity = s },
          label = { Text(s, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (filtered.isEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No Security Anomalies Observed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Zero defensive network anomalies observed on this interface. Monitored indicators: high-bandwidth bursts, unencrypted HTTP cleartext, DNS flooding, and port scans.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filtered) { alert ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = getSeverityColor(alert.severity).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = alert.severity.name,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = getSeverityColor(alert.severity),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(alert.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Text(alert.timeFormatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Target Device: ${alert.deviceIp}  •  Source: ${alert.sourceAddress} → ${alert.destinationAddress}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
              )

              Spacer(modifier = Modifier.height(6.dp))

              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text("Technical Evidence:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                  Text(alert.evidence, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = alert.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
              )
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// 8. NETWORK HISTORY TAB (Requirement Section 10 & 13)
// -----------------------------------------------------------------------------------------
@Composable
private fun NetworkHistoryTab(
  scope: AuthorizedNetworkScope,
  historicalSessions: List<NetworkSessionEntity>
) {
  val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text("Recorded Network Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Text("Isolated historical audit logs from Room SQLite database", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(4.dp))
    }

    if (historicalSessions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No Past Network Sessions Recorded", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Sessions are recorded automatically when network interfaces change or packet capture begins. Each network session is preserved independently in SQLite.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(historicalSessions) { session ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(session.sessionId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(session.captureStatus, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Network: ${session.networkName} (${session.interfaceName})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("Recorded: ${dateFormat.format(Date(session.startTime))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Subnet: ${session.subnet}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Traffic: ${formatBytes(session.totalBytes.toLong())}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
              Text("Packets: ${session.totalPackets}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

// -----------------------------------------------------------------------------------------
// 9. EXPORT TAB (Requirement Section 10 & 13)
// -----------------------------------------------------------------------------------------
@Composable
private fun ExportTab(
  scope: AuthorizedNetworkScope,
  onGenerateExport: (format: String, target: String, timeRange: String) -> String
) {
  val context = LocalContext.current
  var selectedFormat by remember { mutableStateOf("TEXT") }
  var selectedTarget by remember { mutableStateOf("Complete Network Audit") }
  var selectedTimeRange by remember { mutableStateOf("All Time") }
  var generatedOutput by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text("Network Audit & Telemetry Export", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Text("Generate exportable audit reports for ${scope.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(4.dp))

    // Format Selector
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("1. Select Export Format", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("TEXT", "CSV", "JSON").forEach { fmt ->
            FilterChip(
              selected = selectedFormat == fmt,
              onClick = { selectedFormat = fmt },
              label = { Text(fmt, fontWeight = FontWeight.Bold) }
            )
          }
        }
      }
    }

    // Target Data Scope
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("2. Target Scope", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Complete Network Audit", "Connected Devices", "Active Flows").forEach { tgt ->
            FilterChip(
              selected = selectedTarget == tgt,
              onClick = { selectedTarget = tgt },
              label = { Text(tgt, fontSize = 11.sp) }
            )
          }
        }
      }
    }

    // Time Range Selector
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("3. Time Range Filter", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          val timeRanges = listOf("Last 1 Hour", "Last 6 Hours", "Last 24 Hours", "Last 7 Days", "All Time")
          items(timeRanges) { tr ->
            FilterChip(
              selected = selectedTimeRange == tr,
              onClick = { selectedTimeRange = tr },
              label = { Text(tr, fontSize = 11.sp) }
            )
          }
        }
      }
    }

    // Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = {
          generatedOutput = onGenerateExport(selectedFormat, selectedTarget, selectedTimeRange)
        },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Generate Report")
      }

      if (generatedOutput != null) {
        Button(
          onClick = {
            SummaryReportUtils.shareTextReport(context, generatedOutput ?: "", "Network Audit Report ($selectedFormat)")
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Share")
        }
      }
    }

    generatedOutput?.let { out ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Report Preview ($selectedFormat)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            IconButton(
              onClick = {
                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip.setPrimaryClip(ClipData.newPlainText("Network Report", out))
                Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
              }
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report", modifier = Modifier.size(16.dp))
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = out,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            maxLines = 40
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

// -----------------------------------------------------------------------------------------
// MODALS
// -----------------------------------------------------------------------------------------
@Composable
private fun DeviceDetailsModal(
  device: ObservedNetworkDevice,
  scope: AuthorizedNetworkScope,
  allFlows: List<CommunicationFlow>,
  allServices: List<ApplicationServiceAnalysis>,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = device.hostname.ifBlank { "Device: ${device.ipAddress}" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${device.vendor} • ${device.estimatedDeviceType}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Info, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text("Device Network Properties", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        DetailRow("IP Address", device.ipAddress, monospace = true)
        DetailRow("MAC Address", device.macAddress, monospace = true)
        DetailRow("Discovered On", scope.name)
        DetailRow("First Seen", device.firstSeenFormatted)
        DetailRow("Last Active", device.lastSeenFormatted)
        DetailRow("Total Traffic", formatBytes(device.totalBytes))
        DetailRow("Upload", formatBytes(device.uploadBytes))
        DetailRow("Download", formatBytes(device.downloadBytes))
        DetailRow("Packets", "${device.totalPackets}")
        DetailRow("Confidence", device.confidence)

        Spacer(modifier = Modifier.height(10.dp))
        Text("Associated Communication Flows (${allFlows.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        if (allFlows.isEmpty()) {
          Text("No active flows observed for this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          allFlows.take(5).forEach { f ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text("${f.destinationAddress}:${f.port} (${f.protocol})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                if (f.destinationDomain.isNotBlank()) {
                  Text(f.destinationDomain, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Text("${formatBytes(f.totalBytes)} • ${f.packetCount} packets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Close Device Details")
        }
      }
    }
  }
}

@Composable
private fun FlowDetailsModal(
  flow: CommunicationFlow,
  scope: AuthorizedNetworkScope,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text("Observed Flow Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        DetailRow("Source IP", flow.sourceDeviceIp, monospace = true)
        DetailRow("Destination IP", flow.destinationAddress, monospace = true)
        DetailRow("Port", "${flow.port}", monospace = true)
        DetailRow("Protocol", flow.protocol)
        if (flow.destinationDomain.isNotBlank()) {
          DetailRow("Resolved Domain", flow.destinationDomain)
        }
        DetailRow("Total Traffic", formatBytes(flow.totalBytes))
        DetailRow("Packets", "${flow.packetCount}")
        DetailRow("Status", flow.status.name)
        DetailRow("Last Seen", flow.lastSeenFormatted)

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Close Flow Details")
        }
      }
    }
  }
}

// -----------------------------------------------------------------------------------------
// REUSABLE HELPER COMPONENTS
// -----------------------------------------------------------------------------------------
@Composable
private fun InfoChip(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun KpiMetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(color = color.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(24.dp)) {
          Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
          }
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String,
  monospace: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
      textAlign = TextAlign.End
    )
  }
}

@Composable
private fun SecurityAlertMiniCard(alert: DefensiveSecurityAlert) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        color = getSeverityColor(alert.severity).copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text(
          text = alert.severity.name,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = getSeverityColor(alert.severity),
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(alert.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(alert.evidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
    }
  }
}

private fun getSeverityColor(severity: AnomalySeverity): Color {
  return when (severity) {
    AnomalySeverity.HIGH -> Color(0xFFEF4444)
    AnomalySeverity.MEDIUM -> Color(0xFFF59E0B)
    AnomalySeverity.LOW -> Color(0xFF3B82F6)
    AnomalySeverity.INFORMATIONAL -> Color(0xFF10B981)
  }
}

private fun getDeviceIcon(deviceType: DeviceType): ImageVector {
  return when (deviceType) {
    DeviceType.GATEWAY, DeviceType.ROUTER -> Icons.Default.Router
    DeviceType.LAPTOP -> Icons.Default.Laptop
    DeviceType.SMARTPHONE -> Icons.Default.PhoneAndroid
    DeviceType.SERVER -> Icons.Default.Computer
    DeviceType.PRINTER -> Icons.Default.Print
    DeviceType.LOCAL_DEVICE -> Icons.Default.PhoneAndroid
    else -> Icons.Default.Devices
  }
}

private fun formatBytes(bytes: Long): String {
  if (bytes <= 0L) return "0 B"
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
    mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
    kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
    else -> "$bytes B"
  }
}
