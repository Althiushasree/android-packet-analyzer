package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmSeverity
import com.example.data.model.AppTrafficSummary
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.HighestTrafficConsumer
import com.example.data.model.NetworkAlarm
import com.example.data.model.NetworkStats
import com.example.data.model.ProtocolDistribution
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp
import com.example.ui.theme.StatusAlert
import com.example.ui.theme.StatusWarning
import java.util.Locale

@Composable
fun DashboardScreen(
  isCapturing: Boolean,
  stats: NetworkStats,
  protocols: List<ProtocolDistribution>,
  topApps: List<AppTrafficSummary>,
  alarms: List<NetworkAlarm>,
  highestConsumer: HighestTrafficConsumer,
  detailedApps: List<DetailedAppTraffic>,
  detailedIps: List<DetailedIpTraffic>,
  timelineScope: TimelineScope,
  timelinePoints: List<TimelineDataPoint>,
  onToggleCapture: () -> Unit,
  onScopeChanged: (TimelineScope) -> Unit,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onInspectIp: (DetailedIpTraffic) -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .verticalScroll(scrollState)
      .testTag("dashboard_screen")
  ) {
    // Interactive TUN Interface Packet Capture Controller Card
    PacketCaptureToggleCard(
      isCapturing = isCapturing,
      stats = stats,
      onToggleCapture = onToggleCapture
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Real-Time Speed & Bandwidth Metrics Row
    SpeedMetricsRow(stats = stats)

    Spacer(modifier = Modifier.height(16.dp))

    // Core Packet & Flow Analysis Multi-Chart Section
    PacketAnalysisStatsSection(
      highestConsumer = highestConsumer,
      topApps = detailedApps,
      topIps = detailedIps,
      timelineScope = timelineScope,
      timelinePoints = timelinePoints,
      onScopeChanged = onScopeChanged,
      onInspectApp = onInspectApp,
      onInspectIp = onInspectIp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Device Summary (NetFlow V5, V9, NSEL, SFlow, WLC)
    DeviceSummaryCard()

    Spacer(modifier = Modifier.height(16.dp))

    // HeatMap & Alarm Overview
    HeatMapAndAlarmSection(
      alarms = alarms,
      detailedApps = detailedApps,
      detailedIps = detailedIps,
      onInspectApp = onInspectApp,
      onInspectIp = onInspectIp
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Top N Protocol Distribution Donut Chart
    TopProtocolSection(protocols = protocols)

    Spacer(modifier = Modifier.height(24.dp))
  }
}

/**
 * Interactive UI Component providing a prominent Start/Stop toggle
 * integrated directly with the PacketCaptureService and Android TUN VPN lifecycle.
 */
@Composable
fun PacketCaptureToggleCard(
  isCapturing: Boolean,
  stats: NetworkStats,
  onToggleCapture: () -> Unit
) {
  val activeContainerColor = if (isCapturing) {
    MaterialTheme.colorScheme.primaryContainer
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("packet_capture_toggle_card"),
    colors = CardDefaults.cardColors(containerColor = activeContainerColor),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isCapturing) 4.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Top Status Bar and Primary Toggle Switch
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          // Status Indicator Orb
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(
                if (isCapturing) Color(0xFF16A34A)
                else Color(0xFF94A3B8)
              )
          )

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = if (isCapturing) "CAPTURE ACTIVE" else "CAPTURE IDLE",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (isCapturing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
              text = if (isCapturing) "tun0 active • recording" else "VPN tunnel ready",
              style = MaterialTheme.typography.bodySmall,
              color = if (isCapturing) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              maxLines = 1,
              overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
              fontSize = 11.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Prominent Switch Toggle
        Switch(
          checked = isCapturing,
          onCheckedChange = { onToggleCapture() },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF16A34A),
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surface
          ),
          modifier = Modifier.testTag("packet_capture_switch_toggle")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = if (isCapturing) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant)
      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row with Live Telemetry Badges and 1-Tap Action Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          // Packet Count Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCapturing) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(
              width = 1.dp,
              color = if (isCapturing) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
              shape = RoundedCornerShape(8.dp)
            )
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Radio,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isCapturing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "${stats.totalPacketsCaptured} pkts",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                maxLines = 1
              )
            }
          }

          // Data Volume Badge
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCapturing) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(
              width = 1.dp,
              color = if (isCapturing) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
              shape = RoundedCornerShape(8.dp)
            )
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isCapturing) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = String.format(Locale.US, "%.2f MB", stats.totalBytesCaptured / 1024.0 / 1024.0),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                maxLines = 1
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Action Button (Start / Stop)
        Button(
          onClick = onToggleCapture,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCapturing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("toggle_capture_dashboard_button")
        ) {
          Icon(
            imageVector = if (isCapturing) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isCapturing) "Stop VPN Capture" else "Start VPN Capture",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isCapturing) "Stop" else "Start",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun SpeedMetricsRow(stats: NetworkStats) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Download Speed Card
    Card(
      modifier = Modifier.weight(1f),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Download Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "${String.format(Locale.US, "%.1f", stats.downloadSpeedMbps)} MB/s",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    // Upload Speed Card
    Card(
      modifier = Modifier.weight(1f),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Upload Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "${String.format(Locale.US, "%.1f", stats.uploadSpeedMbps)} MB/s",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

@Composable
private fun DeviceSummaryCard() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Device Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Text("NetFlow V9 Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        DeviceMetricItem("V5", "0")
        DeviceMetricItem("V9", "7", isActive = true)
        DeviceMetricItem("NSEL", "0")
        DeviceMetricItem("SFlow", "0")
        DeviceMetricItem("WLC", "1", isActive = true)
      }
    }
  }
}

@Composable
private fun DeviceMetricItem(label: String, value: String, isActive: Boolean = false) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun HeatMapAndAlarmSection(
  alarms: List<NetworkAlarm>,
  detailedApps: List<DetailedAppTraffic>,
  detailedIps: List<DetailedIpTraffic>,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onInspectIp: (DetailedIpTraffic) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // HeatMap Donut & Grid Card
    Card(
      modifier = Modifier.weight(1f),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("HeatMap Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .size(100.dp)
            .align(Alignment.CenterHorizontally)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            drawArc(color = Color(0xFF16A34A), startAngle = 180f, sweepAngle = 120f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = Color(0xFFEAB308), startAngle = 300f, sweepAngle = 90f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            drawArc(color = Color(0xFFDC2626), startAngle = 30f, sweepAngle = 30f, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
          }
          Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("19", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Links", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    // Recent Alarms Card
    Card(
      modifier = Modifier.weight(1f),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Recent Alarms", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
          Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusAlert, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (alarms.isEmpty()) {
          Text("No active security alarms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          alarms.take(3).forEach { alarm ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  val app = detailedApps.find { alarm.title.contains(it.appName, ignoreCase = true) || alarm.message.contains(it.appName, ignoreCase = true) }
                  val ip = detailedIps.find { alarm.title.contains(it.ip) || alarm.message.contains(it.ip) }
                  if (app != null) {
                    onInspectApp(app)
                  } else if (ip != null) {
                    onInspectIp(ip)
                  } else if (detailedApps.isNotEmpty()) {
                    onInspectApp(detailedApps.first())
                  }
                }
                .padding(vertical = 4.dp, horizontal = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(
                    when (alarm.severity) {
                      AlarmSeverity.CRITICAL, AlarmSeverity.HIGH -> StatusAlert
                      AlarmSeverity.WARNING -> StatusWarning
                      AlarmSeverity.MONITOR -> MaterialTheme.colorScheme.tertiary
                      AlarmSeverity.NORMAL, AlarmSeverity.INFO -> MaterialTheme.colorScheme.primary
                    }
                  )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(alarm.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(alarm.timeFormatted, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TopProtocolSection(protocols: List<ProtocolDistribution>) {
  var selectedProtocolForDetail by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ProtocolDistribution?>(null) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Top N Protocol Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(12.dp))

      if (protocols.isEmpty()) {
        Text("No network traffic captured yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      } else {
        protocols.forEach { dist ->
          val color = when (dist.protocol) {
            "TCP" -> ProtocolTcp
            "UDP" -> ProtocolUdp
            "DNS" -> ProtocolDns
            "TLS" -> ProtocolTls
            "HTTP" -> ProtocolHttp
            "QUIC" -> ProtocolQuic
            else -> ProtocolOther
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .clickable { selectedProtocolForDetail = dist }
              .padding(vertical = 4.dp, horizontal = 2.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Text(dist.protocol, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              }
              Text(
                text = "${String.format(Locale.US, "%.1f", dist.percentage)}% (${dist.count} pkts)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { (dist.percentage / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
              color = color,
              trackColor = color.copy(alpha = 0.15f)
            )
          }
        }
      }
    }
  }

  selectedProtocolForDetail?.let { proto ->
    androidx.compose.ui.window.Dialog(onDismissRequest = { selectedProtocolForDetail = null }) {
      Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${proto.protocol} Protocol Details",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.IconButton(onClick = { selectedProtocolForDetail = null }) {
              Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Total Packets: ${proto.count}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Traffic Share: ${String.format(Locale.US, "%.1f", proto.percentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when (proto.protocol) {
              "TCP" -> "Transmission Control Protocol (OSI Layer 4 Connection-Oriented Flow)"
              "UDP" -> "User Datagram Protocol (OSI Layer 4 Connectionless Low-Latency Streaming)"
              "TLS" -> "Transport Layer Security (Encrypted Handshake & Crypto Tunnel)"
              "DNS" -> "Domain Name System (Port 53 Name Resolution Queries & Responses)"
              "HTTP" -> "Hypertext Transfer Protocol (Cleartext Web Traffic)"
              "QUIC" -> "Quick UDP Internet Connections (HTTP/3 Multiplexed Transport)"
              else -> "Standard Transport & Application Protocol"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))
          androidx.compose.material3.Button(
            onClick = { selectedProtocolForDetail = null },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Done")
          }
        }
      }
    }
  }
}
