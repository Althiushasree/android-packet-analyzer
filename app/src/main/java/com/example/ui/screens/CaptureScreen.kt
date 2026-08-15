package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkStats
import com.example.data.model.PacketEntity
import com.example.ui.components.ExpandablePacketCaptureLog
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
  isCapturing: Boolean,
  isPaused: Boolean,
  stats: NetworkStats,
  activeInterface: String,
  promiscuousMode: Boolean,
  captureFilter: String,
  isCaptureFilterValid: Boolean,
  fileFormat: String,
  ringBufferSizeMb: Int,
  snapLength: Int,
  recentPackets: List<PacketEntity>,
  onToggleCapture: () -> Unit,
  onPauseResume: () -> Unit,
  onClearPackets: () -> Unit,
  onExportPcap: () -> Unit,
  onSelectInterface: (String) -> Unit,
  onTogglePromiscuous: (Boolean) -> Unit,
  onChangeFilter: (String) -> Unit,
  onSelectFileFormat: (String) -> Unit,
  onSelectRingBuffer: (Int) -> Unit,
  onSelectSnapLength: (Int) -> Unit,
  onPacketClick: (PacketEntity) -> Unit
) {
  var showOptionsPanel by remember { mutableStateOf(false) }
  var ifaceDropdownExpanded by remember { mutableStateOf(false) }
  val interfaces = listOf("wlan0 (Wi-Fi)", "tun0 (VPN)", "rmnet_data0 (Cellular)", "lo (Loopback)")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
      .testTag("capture_screen")
  ) {
    // Top Demo Mode Banner
    DemoModeBanner()

    Spacer(modifier = Modifier.height(12.dp))

    // Capture Status Header Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("capture_status_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isCapturing && !isPaused -> Color(0xFF16A34A)
                    isPaused -> Color(0xFFF59E0B)
                    else -> Color(0xFF64748B)
                  }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = when {
                isCapturing && !isPaused -> "CAPTURE ACTIVE"
                isPaused -> "CAPTURE PAUSED"
                else -> "READY TO CAPTURE"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = when {
                isCapturing && !isPaused -> Color(0xFF16A34A)
                isPaused -> Color(0xFFF59E0B)
                else -> Color(0xFF0F172A)
              }
            )
          }

          // Interface Tag
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(activeInterface.substringBefore(" "), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid (Packets, Data, Duration, Rate)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CaptureKpiBox("Packets", "${stats.totalPacketsCaptured}", Modifier.weight(1f))
          CaptureKpiBox("Data Size", "${String.format(Locale.US, "%.2f", stats.totalBytesCaptured / 1024.0 / 1024.0)} MB", Modifier.weight(1f))
          CaptureKpiBox("Duration", formatDuration(stats.durationSeconds), Modifier.weight(1f))
          CaptureKpiBox("Throughput", "${String.format(Locale.US, "%.1f", stats.downloadSpeedMbps + stats.uploadSpeedMbps)} MB/s", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Controls Row (START, STOP, PAUSE, CLEAR, EXPORT)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onToggleCapture,
            modifier = Modifier.weight(1.5f).testTag("capture_start_stop_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCapturing) Color(0xFFDC2626) else Color(0xFF2563EB)
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(if (isCapturing) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isCapturing) "STOP" else "START CAPTURE", fontWeight = FontWeight.Bold)
          }

          if (isCapturing) {
            OutlinedButton(
              onClick = onPauseResume,
              modifier = Modifier.weight(1f).testTag("capture_pause_resume_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isPaused) "Resume" else "Pause")
            }
          }

          OutlinedButton(
            onClick = onClearPackets,
            modifier = Modifier.weight(1f).testTag("capture_clear_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear")
          }

          IconButton(
            onClick = onExportPcap,
            modifier = Modifier.size(40.dp).testTag("capture_export_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = "Export PCAP", tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Capture Options Expandable Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("capture_options_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth().clickable { showOptionsPanel = !showOptionsPanel },
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capture Options & BPF Filters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
          Text(if (showOptionsPanel) "Collapse" else "Configure", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }

        AnimatedVisibility(visible = showOptionsPanel) {
          Column(modifier = Modifier.padding(top = 12.dp)) {
            // Interface Selector
            ExposedDropdownMenuBox(
              expanded = ifaceDropdownExpanded,
              onExpandedChange = { ifaceDropdownExpanded = !ifaceDropdownExpanded },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                value = activeInterface,
                onValueChange = {},
                readOnly = true,
                label = { Text("Active Network Interface") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ifaceDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = ifaceDropdownExpanded,
                onDismissRequest = { ifaceDropdownExpanded = false }
              ) {
                interfaces.forEach { iface ->
                  DropdownMenuItem(
                    text = { Text(iface) },
                    onClick = {
                      onSelectInterface(iface)
                      ifaceDropdownExpanded = false
                    }
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Promiscuous Mode Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Promiscuous Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Capture all incoming and transit packets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = promiscuousMode,
                onCheckedChange = onTogglePromiscuous,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capture Filter with Real-time Validation Indicator
            OutlinedTextField(
              value = captureFilter,
              onValueChange = onChangeFilter,
              label = { Text("Capture Filter (BPF / Wireshark Syntax)") },
              placeholder = { Text("e.g. tcp.port == 443 or dns") },
              modifier = Modifier.fillMaxWidth().testTag("capture_filter_input"),
              trailingIcon = {
                if (captureFilter.isNotEmpty()) {
                  if (isCaptureFilterValid) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid Syntax", tint = Color(0xFF16A34A))
                  } else {
                    Icon(Icons.Default.Error, contentDescription = "Invalid Syntax", tint = Color(0xFFDC2626))
                  }
                }
              },
              shape = RoundedCornerShape(10.dp),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Buffer Size & Snaplen Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = "$ringBufferSizeMb MB",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ring Buffer") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = "$snapLength B",
                onValueChange = {},
                readOnly = true,
                label = { Text("Snap Length") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = fileFormat,
                onValueChange = {},
                readOnly = true,
                label = { Text("Format") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Live Packet Feed Preview
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Live Stream Preview (${recentPackets.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Tap row to expand metadata",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    ExpandablePacketCaptureLog(
      packets = recentPackets.take(10),
      onPacketInspect = onPacketClick,
      emptyMessage = "Press START CAPTURE to stream packets"
    )

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
fun DemoModeBanner() {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("demo_mode_banner"),
    color = Color(0xFFFEF3C7),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "DEMO MODE — SIMULATED NETWORK TRAFFIC",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF92400E)
        )
        Text(
          text = "Emulating live kernel packet socket capture with real flow analysis",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFFB45309)
        )
      }
    }
  }
}

@Composable
private fun CaptureKpiBox(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(10.dp)
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
  }
}

@Composable
private fun LivePacketFeedItem(packet: PacketEntity, onClick: () -> Unit) {
  val protoColor = when (packet.protocol) {
    "TCP" -> ProtocolTcp
    "UDP" -> ProtocolUdp
    "DNS" -> ProtocolDns
    "TLS" -> ProtocolTls
    "HTTP" -> ProtocolHttp
    "QUIC" -> ProtocolQuic
    else -> ProtocolOther
  }

  Surface(
    modifier = Modifier.fillMaxWidth().clickable { onClick() },
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(8.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(protoColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = packet.timeFormatted,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${packet.sourceIp}:${packet.sourcePort} → ${packet.destIp}:${packet.destPort}",
          style = MaterialTheme.typography.bodySmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
      }

      Surface(
        color = protoColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
      ) {
        Text(
          text = packet.protocol,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = protoColor,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}

private fun formatDuration(seconds: Long): String {
  val h = seconds / 3600
  val m = (seconds % 3600) / 60
  val s = seconds % 60
  return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
  else String.format(Locale.US, "%02d:%02d", m, s)
}
