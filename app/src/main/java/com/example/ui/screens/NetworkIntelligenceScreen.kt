package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Psychology
import com.example.data.intelligence.AiAnalystInsight
import com.example.data.intelligence.AnomalySeverity
import com.example.data.intelligence.ApplicationServiceAnalysis
import com.example.data.intelligence.CommunicationFlow
import com.example.data.intelligence.DefensiveSecurityAlert
import com.example.data.intelligence.DeviceType
import com.example.data.intelligence.IntelligenceStatus
import com.example.data.intelligence.NetworkHealthReport
import com.example.data.intelligence.ObservedNetworkDevice
import com.example.data.intelligence.RealDnsLogEntry
import com.example.data.intelligence.RealNetworkInterfaceInfo
import com.example.data.intelligence.RealTimeTrafficStats
import com.example.data.ml.MlInferenceResult
import com.example.data.ml.MlModelHealthState
import com.example.ui.components.RealTimeNetworkGraph
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NetworkIntelligenceScreen(
  networkInfo: RealNetworkInterfaceInfo,
  availableInterfaces: List<String>,
  networkChangeBanner: String?,
  observedDevices: List<ObservedNetworkDevice>,
  communicationFlows: List<CommunicationFlow>,
  applicationServices: List<ApplicationServiceAnalysis>,
  dnsLogs: List<RealDnsLogEntry>,
  liveTrafficStats: RealTimeTrafficStats,
  networkHealth: NetworkHealthReport,
  securityAlerts: List<DefensiveSecurityAlert>,
  aiAnalystInsight: AiAnalystInsight,
  mlModelHealth: MlModelHealthState = MlModelHealthState(),
  mlInferences: List<MlInferenceResult> = emptyList(),
  isMonitoringActive: Boolean,
  isDiscoveryScanning: Boolean,
  onDismissNetworkChangeBanner: () -> Unit,
  onToggleMonitoring: () -> Unit,
  onTriggerDiscovery: () -> Unit,
  onClearData: () -> Unit,
  onSelectInterface: (String) -> Unit,
  onSelectDeviceForDeepAnalysis: (ObservedNetworkDevice) -> Unit,
  onGenerateAiAnalysis: () -> Unit,
  onRetrainMlModel: () -> Unit = {},
  onUpdateContaminationThreshold: (Double) -> Unit = {},
  onTestMlFlow: (Double, Double, Double, Double, Double) -> Unit = { _, _, _, _, _ -> },
  onExportReport: (String) -> Unit
) {
  var selectedSubViewIndex by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  var interfaceDropdownExpanded by remember { mutableStateOf(false) }

  val subViewTitles = listOf(
    "Overview" to Icons.Default.Public,
    "Devices" to Icons.Default.Devices,
    "Live Traffic" to Icons.Default.Speed,
    "ML Models" to Icons.Default.Psychology,
    "Services" to Icons.Default.Hub,
    "DNS Log" to Icons.Default.Dns,
    "Protocols" to Icons.Default.BarChart,
    "Top Talkers" to Icons.Default.Analytics,
    "Network Graph" to Icons.Default.DeviceHub,
    "Health" to Icons.Default.HealthAndSafety,
    "Security" to Icons.Default.Security,
    "AI Analyst" to Icons.Default.AutoAwesome
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp)
      .testTag("network_intelligence_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. TOP CONTROL BAR (START / PAUSE / STOP / SCAN / CLEAR / INTERFACE)
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = if (isMonitoringActive) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                shape = CircleShape,
                modifier = Modifier.size(10.dp)
              ) {}
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isMonitoringActive) "REAL-TIME MONITORING" else "MONITORING PAUSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMonitoringActive) Color(0xFF10B981) else MaterialTheme.colorScheme.error
              )
            }

            // Interface Selector Button
            Box {
              OutlinedButton(
                onClick = { interfaceDropdownExpanded = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .height(34.dp)
                  .testTag("network_intel_interface_selector")
              ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = networkInfo.interfaceName,
                  fontSize = 11.sp,
                  maxLines = 1
                )
              }

              DropdownMenu(
                expanded = interfaceDropdownExpanded,
                onDismissRequest = { interfaceDropdownExpanded = false }
              ) {
                availableInterfaces.forEach { iface ->
                  DropdownMenuItem(
                    text = { Text(iface, fontSize = 12.sp) },
                    onClick = {
                      onSelectInterface(iface.split(" ").first())
                      interfaceDropdownExpanded = false
                    }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Control Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onToggleMonitoring,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isMonitoringActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isMonitoringActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("intel_toggle_monitor_btn")
            ) {
              Icon(
                imageVector = if (isMonitoringActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isMonitoringActive) "Pause" else "Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = onTriggerDiscovery,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
              ),
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("intel_rescan_subnet_btn")
            ) {
              if (isDiscoveryScanning) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              }
              Spacer(modifier = Modifier.width(4.dp))
              Text("Scan Subnet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = onClearData,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .height(36.dp)
                .testTag("intel_clear_data_btn")
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // 2. NETWORK CHANGED BANNER (Conditional)
    if (!networkChangeBanner.isNullOrBlank()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = networkChangeBanner,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
            }
            IconButton(onClick = onDismissNetworkChangeBanner, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }

    // 3. NETWORK VISIBILITY WARNING
    item {
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .size(18.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PARTIAL NETWORK VISIBILITY: The current network configuration prevents complete visibility of all connected devices or traffic. Only devices and traffic that are actually observable are shown.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp
          )
        }
      }
    }

    // 4. SUB-VIEW CATEGORY CHIPS
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(subViewTitles.indices.toList()) { index ->
          val (title, icon) = subViewTitles[index]
          FilterChip(
            selected = selectedSubViewIndex == index,
            onClick = { selectedSubViewIndex = index },
            label = { Text(title, fontSize = 12.sp, fontWeight = if (selectedSubViewIndex == index) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("intel_subtab_$index")
          )
        }
      }
    }

    // 5. SUB-VIEW CONTENT SECTIONS
    when (selectedSubViewIndex) {
      0 -> { // 1. Network Overview
        item {
          NetworkOverviewSection(networkInfo = networkInfo)
        }
      }
      1 -> { // 2. Devices (Observed Count + Table)
        item {
          DevicesObservedSection(
            observedDevices = observedDevices,
            onDeviceClick = onSelectDeviceForDeepAnalysis,
            isScanning = isDiscoveryScanning,
            onRescan = onTriggerDiscovery
          )
        }
      }
      2 -> { // 3. Live Packets & Traffic
        item {
          LiveTrafficSection(stats = liveTrafficStats, flows = communicationFlows)
        }
      }
      3 -> { // 4. On-Device Machine Learning Models & Intrusion Detection
        item {
          MlModelsSection(
            modelHealth = mlModelHealth,
            inferences = mlInferences,
            onRetrain = onRetrainMlModel,
            onUpdateThreshold = onUpdateContaminationThreshold,
            onRunTest = onTestMlFlow
          )
        }
      }
      4 -> { // 5. Applications & Services
        item {
          ApplicationsServicesSection(services = applicationServices)
        }
      }
      5 -> { // 6. DNS Logs
        item {
          DnsLogSection(dnsLogs = dnsLogs)
        }
      }
      6 -> { // 7. Protocols Distribution
        item {
          ProtocolAnalysisSection(stats = liveTrafficStats)
        }
      }
      7 -> { // 8. Top Talkers & Destinations
        item {
          TopTalkersDestinationsSection(devices = observedDevices, flows = communicationFlows)
        }
      }
      8 -> { // 9. Network Graph
        item {
          NetworkGraphSection(networkInfo = networkInfo, devices = observedDevices, flows = communicationFlows)
        }
      }
      9 -> { // 10. Network Health
        item {
          NetworkHealthSection(health = networkHealth, onRefresh = onTriggerDiscovery)
        }
      }
      10 -> { // 11. Security Anomaly Center
        item {
          DefensiveSecuritySection(alerts = securityAlerts)
        }
      }
      11 -> { // 12. AI Network Analyst
        item {
          AiAnalystSection(
            insight = aiAnalystInsight,
            onGenerate = onGenerateAiAnalysis
          )
        }
      }
    }

    // 6. EXPORT / REPORT FOOTER
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Export Network Intelligence", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Export observed devices, flows and metrics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Button(
            onClick = { onExportReport("JSON") },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("intel_export_report_btn")
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export", fontSize = 12.sp)
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

// -------------------------------------------------------------
// SECTION 1: NETWORK OVERVIEW
// -------------------------------------------------------------
@Composable
private fun NetworkOverviewSection(networkInfo: RealNetworkInterfaceInfo) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("NETWORK INTELLIGENCE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(networkInfo.ssid, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Surface(
            color = if (networkInfo.isConnected) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = if (networkInfo.isConnected) "● Connected" else "○ Disconnected",
              color = if (networkInfo.isConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        InfoGridRow("Interface", networkInfo.interfaceName, "Type", networkInfo.interfaceType)
        InfoGridRow("Local IPv4", networkInfo.localIpv4, "Subnet Mask", networkInfo.subnetMask, isMono = true)
        InfoGridRow("Default Gateway", networkInfo.defaultGateway, "DNS Servers", networkInfo.dnsServers.joinToString(", "), isMono = true)
        InfoGridRow("IPv6 Address", networkInfo.localIpv6, "MAC Address", networkInfo.macAddress, isMono = true)
        InfoGridRow("MTU", "${networkInfo.mtu} bytes", "Link Speed", if (networkInfo.linkSpeedMbps > 0) "${networkInfo.linkSpeedMbps} Mbps" else "Not observable")
        InfoGridRow("DHCP Server", networkInfo.dhcpServer, "Duration", formatDuration(networkInfo.connectionDurationSeconds))
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 2: DEVICES OBSERVED
// -------------------------------------------------------------
@Composable
private fun DevicesObservedSection(
  observedDevices: List<ObservedNetworkDevice>,
  onDeviceClick: (ObservedNetworkDevice) -> Unit,
  isScanning: Boolean,
  onRescan: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Large Count Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "DEVICES OBSERVED",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
              text = "${observedDevices.size}",
              fontSize = 38.sp,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }

          if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.primary)
          } else {
            IconButton(
              onClick = onRescan,
              modifier = Modifier.testTag("rescan_devices_btn")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Rescan Subnet", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "${observedDevices.size} devices observed on active local subnet.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
        Text(
          text = "Note: The network may contain additional devices that are not visible because of client isolation, firewall rules, permissions, or network segmentation.",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 10.5.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
          lineHeight = 14.sp
        )
      }
    }

    // Devices Table
    Text(
      text = "CONNECTED / OBSERVED DEVICES",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )

    if (observedDevices.isEmpty()) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("No devices actively observable on current subnet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      observedDevices.forEach { device ->
        DeviceListItemCard(device = device, onClick = { onDeviceClick(device) })
      }
    }
  }
}

@Composable
private fun DeviceListItemCard(
  device: ObservedNetworkDevice,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("device_row_${device.ipAddress}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Surface(
          shape = CircleShape,
          color = if (device.isLocalHost) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(38.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = when (device.estimatedDeviceType) {
                DeviceType.ROUTER, DeviceType.GATEWAY, DeviceType.ACCESS_POINT -> Icons.Default.Router
                DeviceType.SMARTPHONE -> Icons.Default.Laptop
                DeviceType.LOCAL_DEVICE -> Icons.Default.Computer
                else -> Icons.Default.Laptop
              },
              contentDescription = null,
              tint = if (device.isLocalHost) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = device.ipAddress,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            if (device.isLocalHost) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                Text("Self", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
              }
            } else if (device.isGateway) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(4.dp)) {
                Text("Gateway", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
              }
            }
          }
          Text(
            text = "${device.vendor} • ${device.estimatedDeviceType.name.replace("_", " ")}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
          Text(
            text = "MAC: ${device.macAddress}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Surface(
          color = if (device.isActive) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = if (device.isActive) "● Active" else "○ Inactive",
            color = if (device.isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = formatBytes(device.totalBytes),
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 3: LIVE TRAFFIC
// -------------------------------------------------------------
@Composable
private fun LiveTrafficSection(stats: RealTimeTrafficStats, flows: List<CommunicationFlow>) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("LIVE TRAFFIC METRICS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          MetricBox(Modifier.weight(1f), "Packets/sec", String.format(Locale.US, "%.1f", stats.packetsPerSec), Icons.Default.Bolt, MaterialTheme.colorScheme.primary)
          MetricBox(Modifier.weight(1f), "Throughput", formatThroughput(stats.bytesPerSec), Icons.Default.Speed, MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          MetricBox(Modifier.weight(1f), "Upload Rate", formatThroughput(stats.uploadBytesPerSec), Icons.Default.ArrowUpward, MaterialTheme.colorScheme.tertiary)
          MetricBox(Modifier.weight(1f), "Download Rate", formatThroughput(stats.downloadBytesPerSec), Icons.Default.ArrowDownward, Color(0xFF10B981))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          MetricBox(Modifier.weight(1f), "Total Packets", stats.totalPackets.toString(), Icons.Default.BarChart, MaterialTheme.colorScheme.onSurface)
          MetricBox(Modifier.weight(1f), "Total Data", formatBytes(stats.totalBytes), Icons.Default.Public, MaterialTheme.colorScheme.primary)
        }
      }
    }

    // Active Sockets / Flows
    Text("ACTIVE SOCKET FLOWS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    if (flows.isEmpty()) {
      Text("No active outgoing communication flows currently recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      flows.take(15).forEach { flow ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${flow.sourceDeviceIp} ➔ ${flow.destinationAddress}:${flow.port}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "Protocol: ${flow.protocol} • Last: ${flow.lastSeenFormatted}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = formatBytes(flow.totalBytes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 4: APPLICATIONS & SERVICES
// -------------------------------------------------------------
@Composable
private fun ApplicationsServicesSection(services: List<ApplicationServiceAnalysis>) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("APPLICATIONS & SERVICES ANALYSIS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Classification Rules:\n• OBSERVED: Directly visible in actual data\n• INFERRED: Estimated from DNS/domain/IP metadata\n• UNKNOWN: Metadata insufficient due to encryption",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 16.sp
        )
      }
    }

    if (services.isEmpty()) {
      Text("No application services actively observed on current socket streams.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      services.forEach { svc ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(svc.serviceName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Surface(
                color = when (svc.status) {
                  IntelligenceStatus.OBSERVED -> Color(0xFF10B981).copy(alpha = 0.15f)
                  IntelligenceStatus.INFERRED -> MaterialTheme.colorScheme.primaryContainer
                  IntelligenceStatus.UNKNOWN -> MaterialTheme.colorScheme.errorContainer
                },
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = svc.status.name,
                  color = when (svc.status) {
                    IntelligenceStatus.OBSERVED -> Color(0xFF10B981)
                    IntelligenceStatus.INFERRED -> MaterialTheme.colorScheme.primary
                    IntelligenceStatus.UNKNOWN -> MaterialTheme.colorScheme.error
                  },
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Evidence: ${svc.evidence}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Traffic: ${formatBytes(svc.trafficBytes)} • Packets: ${svc.packetCount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            if (svc.explanation.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(svc.explanation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 5: DNS LOGS
// -------------------------------------------------------------
@Composable
private fun DnsLogSection(dnsLogs: List<RealDnsLogEntry>) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("REAL-TIME DNS ANALYSIS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

    if (dnsLogs.isEmpty()) {
      Text("No DNS lookup requests recorded yet. Querying active DNS servers...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      dnsLogs.reversed().forEach { log ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${log.queryDomain} (${log.queryType})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Server: ${log.dnsServer} • Latency: ${log.latencyMs} ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (log.responseAnswer.isNotEmpty()) {
                Text(
                  text = "Answer: ${log.responseAnswer}",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
            Text(log.timeFormatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 6: PROTOCOLS
// -------------------------------------------------------------
@Composable
private fun ProtocolAnalysisSection(stats: RealTimeTrafficStats) {
  val total = maxOf(1L, stats.totalPackets)
  val tcpPct = ((stats.tcpPackets.toFloat() / total) * 100).roundToInt()
  val udpPct = ((stats.udpPackets.toFloat() / total) * 100).roundToInt()
  val tlsPct = ((stats.tlsPackets.toFloat() / total) * 100).roundToInt()
  val quicPct = ((stats.quicPackets.toFloat() / total) * 100).roundToInt()
  val dnsPct = ((stats.dnsPackets.toFloat() / total) * 100).roundToInt()
  val icmpPct = ((stats.icmpPackets.toFloat() / total) * 100).roundToInt()

  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("REAL-TIME PROTOCOL DISTRIBUTION", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        ProtocolBar("TCP Stream", tcpPct, MaterialTheme.colorScheme.primary)
        ProtocolBar("UDP Datagram", udpPct, MaterialTheme.colorScheme.secondary)
        ProtocolBar("TLS / HTTPS", tlsPct, Color(0xFF10B981))
        ProtocolBar("QUIC / HTTP3", quicPct, MaterialTheme.colorScheme.tertiary)
        ProtocolBar("DNS Resolution", dnsPct, Color(0xFFF59E0B))
        ProtocolBar("ICMP / Ping", icmpPct, MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
private fun ProtocolBar(label: String, pct: Int, color: Color) {
  Column {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
      Text("$pct%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { (pct / 100f).coerceIn(0f, 1f) },
      color = color,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp),
    )
  }
}

// -------------------------------------------------------------
// SECTION 7: TOP TALKERS & DESTINATIONS
// -------------------------------------------------------------
@Composable
private fun TopTalkersDestinationsSection(
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("TOP TALKERS (DEVICES BY TRAFFIC)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    devices.take(5).forEachIndexed { index, dev ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${index + 1}. ${dev.ipAddress} (${dev.vendor})",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = formatBytes(dev.totalBytes),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("TOP DESTINATIONS OBSERVED", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    flows.take(5).forEachIndexed { index, flow ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${index + 1}. ${flow.destinationAddress}:${flow.port}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
            Text("Protocol: ${flow.protocol}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(
            text = formatBytes(flow.totalBytes),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 8: NETWORK GRAPH
// -------------------------------------------------------------
@Composable
private fun NetworkGraphSection(
  networkInfo: RealNetworkInterfaceInfo,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("REAL-TIME COMMUNICATION GRAPH", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    RealTimeNetworkGraph(
      networkInfo = networkInfo,
      devices = devices,
      flows = flows
    )
  }
}

// -------------------------------------------------------------
// SECTION 9: NETWORK HEALTH
// -------------------------------------------------------------
@Composable
private fun NetworkHealthSection(health: NetworkHealthReport, onRefresh: () -> Unit) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("NETWORK HEALTH SCORE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          Text("${health.healthScore}/100", fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (health.healthScore >= 80) Color(0xFF10B981) else Color(0xFFF59E0B))
        }
        IconButton(onClick = onRefresh) {
          Icon(Icons.Default.Refresh, contentDescription = "Refresh Health")
        }
      }

      Text(health.statusSummary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      HealthRow("Gateway Latency", if (health.gatewayLatencyMs > 0) "${health.gatewayLatencyMs} ms" else "Not available")
      HealthRow("DNS Latency", if (health.dnsLatencyMs > 0) "${health.dnsLatencyMs} ms" else "Not available")
      HealthRow("Packet Loss", "${health.packetLossPercent}%")
      HealthRow("Throughput", "${health.throughputMbps} Mbps")
      HealthRow("Retransmissions", "${health.retransmissionCount}")
      HealthRow("Stability Level", health.stabilityLevel)
    }
  }
}

@Composable
private fun HealthRow(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
  }
}

// -------------------------------------------------------------
// SECTION 10: SECURITY ALERTS
// -------------------------------------------------------------
@Composable
private fun DefensiveSecuritySection(alerts: List<DefensiveSecurityAlert>) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("DEFENSIVE SECURITY ANALYSIS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

    if (alerts.isEmpty()) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("🟢 Defensive Profile Nominal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("No active port scanning, unencrypted streams, or ARP anomalies observed.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      alerts.reversed().forEach { alert ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${when (alert.severity) {
                  AnomalySeverity.INFORMATIONAL -> "🟢"
                  AnomalySeverity.LOW -> "🟡"
                  AnomalySeverity.MEDIUM -> "🟠"
                  AnomalySeverity.HIGH -> "🔴"
                }} ${alert.title}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(alert.timeFormatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Evidence: ${alert.evidence}", style = MaterialTheme.typography.bodySmall)
            Text("Confidence: ${alert.confidence} • Target: ${alert.destinationAddress}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(alert.explanation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// SECTION 11: AI NETWORK ANALYST
// -------------------------------------------------------------
@Composable
private fun AiAnalystSection(
  insight: AiAnalystInsight,
  onGenerate: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("AI NETWORK ANALYST", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = onGenerate,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("ai_analyst_generate_btn")
        ) {
          if (insight.isGenerating) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
          } else {
            Text("Synthesize", fontSize = 12.sp)
          }
        }
      }

      if (insight.networkSummary.isNotEmpty()) {
        Text("Current Network:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(insight.networkSummary, style = MaterialTheme.typography.bodySmall)

        Text("Observable Devices:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(insight.observableDevicesInsight, style = MaterialTheme.typography.bodySmall)

        Text("Top Services & Traffic:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(insight.topServicesInsight, style = MaterialTheme.typography.bodySmall)

        Text("Security Findings:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(insight.securityFindings, style = MaterialTheme.typography.bodySmall)

        Text("Health Assessment:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(insight.healthAssessment, style = MaterialTheme.typography.bodySmall)

        if (insight.recommendations.isNotEmpty()) {
          Text("Defensive Recommendations:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          insight.recommendations.forEach { rec ->
            Text("• $rec", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      } else {
        Text("Click 'Synthesize' to generate deep AI intelligence from actual collected operating system metrics.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

// -------------------------------------------------------------
// COMMON UI HELPERS
// -------------------------------------------------------------
@Composable
private fun InfoGridRow(
  l1: String, v1: String,
  l2: String, v2: String,
  isMono: Boolean = false
) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    Column(modifier = Modifier.weight(1f)) {
      Text(l1, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(v1.ifEmpty { "Not observable" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default)
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(l2, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(v2.ifEmpty { "Not observable" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default)
    }
  }
}

@Composable
private fun MetricBox(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
  }
}

private fun formatBytes(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
    mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
    kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
    else -> "$bytes B"
  }
}

private fun formatThroughput(bytesPerSec: Double): String {
  val bitsPerSec = bytesPerSec * 8.0
  val kbps = bitsPerSec / 1000.0
  val mbps = kbps / 1000.0
  return when {
    mbps >= 1.0 -> String.format(Locale.US, "%.2f Mbps", mbps)
    kbps >= 1.0 -> String.format(Locale.US, "%.1f Kbps", kbps)
    else -> String.format(Locale.US, "%.0f bps", bitsPerSec)
  }
}

private fun formatDuration(sec: Long): String {
  val m = sec / 60
  val s = sec % 60
  val h = m / 60
  return if (h > 0) "${h}h ${m % 60}m" else "${m}m ${s}s"
}

@Composable
private fun MlModelsSection(
  modelHealth: MlModelHealthState,
  inferences: List<MlInferenceResult>,
  onRetrain: () -> Unit,
  onUpdateThreshold: (Double) -> Unit,
  onRunTest: (Double, Double, Double, Double, Double) -> Unit
) {
  var testRate by remember { mutableStateOf("150") }
  var testBytes by remember { mutableStateOf("2500000") }
  var testEntropy by remember { mutableStateOf("7.4") }
  var testJitter by remember { mutableStateOf("4.2") }
  var testPortRisk by remember { mutableStateOf("0.85") }

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // 1. Model Engine Overview Card
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("On-Device ML Intrusion Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("Real-Time Behavioral Flow Classification", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Surface(
            color = Color(0xFF10B981).copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = if (modelHealth.isTrained) "MODEL ACTIVE" else "UNTRAINED",
              color = Color(0xFF10B981),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Model Architecture Metrics
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          MetricBox(
            modifier = Modifier.weight(1f),
            title = "Isolation Trees",
            value = "${modelHealth.isolationForestTrees}",
            icon = Icons.Default.Hub,
            color = MaterialTheme.colorScheme.primary
          )
          MetricBox(
            modifier = Modifier.weight(1f),
            title = "K-Means Clusters",
            value = "${modelHealth.kMeansClusters}",
            icon = Icons.Default.BarChart,
            color = Color(0xFF8B5CF6)
          )
          MetricBox(
            modifier = Modifier.weight(1f),
            title = "Inference Speed",
            value = "${modelHealth.avgInferenceLatencyMs} ms",
            icon = Icons.Default.Speed,
            color = Color(0xFF10B981)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          MetricBox(
            modifier = Modifier.weight(1f),
            title = "Total Inferences",
            value = "${modelHealth.totalInferences}",
            icon = Icons.Default.Analytics,
            color = MaterialTheme.colorScheme.tertiary
          )
          MetricBox(
            modifier = Modifier.weight(1f),
            title = "Anomalies Caught",
            value = "${modelHealth.totalAnomaliesDetected}",
            icon = Icons.Default.Warning,
            color = if (modelHealth.totalAnomaliesDetected > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action: Retrain / Baseline Calibration
        Button(
          onClick = onRetrain,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Recalibrate ML Baseline & Retrain Ensembles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // 2. Interactive Flow Simulation & Testing Card
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text("Interactive ML Model Evaluator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Simulate flow attributes to test real-time classification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = testRate,
            onValueChange = { testRate = it },
            label = { Text("Rate (pkts/s)", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
          )
          OutlinedTextField(
            value = testEntropy,
            onValueChange = { testEntropy = it },
            label = { Text("Entropy (0-8)", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
          )
          OutlinedTextField(
            value = testPortRisk,
            onValueChange = { testPortRisk = it },
            label = { Text("Port Risk (0-1)", fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = {
            val r = testRate.toDoubleOrNull() ?: 100.0
            val b = testBytes.toDoubleOrNull() ?: 1000000.0
            val e = testEntropy.toDoubleOrNull() ?: 5.0
            val j = testJitter.toDoubleOrNull() ?: 10.0
            val p = testPortRisk.toDoubleOrNull() ?: 0.5
            onRunTest(r, b, e, j, p)
          },
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Run Real-Time ML Classification Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    // 3. Live ML Inferences Feed
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Live ML Inference Stream", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text("${inferences.size} flows evaluated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (inferences.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Evaluating live network flows...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            inferences.take(10).forEach { item ->
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = if (item.isAnomaly) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = item.threatClassification,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Bold,
                      color = if (item.isAnomaly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                      color = if (item.anomalyScore > 0.65) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                      shape = RoundedCornerShape(4.dp)
                    ) {
                      Text(
                        text = "Score: ${String.format(Locale.US, "%.2f", item.anomalyScore)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Cluster: ${item.clusterLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )

                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "Threat Prob: ${String.format(Locale.US, "%.1f", item.threatProbability * 100)}%",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = if (item.threatProbability > 0.6) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Latency: ${String.format(Locale.US, "%.2f", item.inferenceLatencyMs)} ms",
                      fontSize = 11.sp,
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
}

