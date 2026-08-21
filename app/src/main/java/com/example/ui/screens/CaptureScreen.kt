package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkStats
import com.example.data.model.PacketEntity
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
  activeCrossFilter: com.example.data.model.ActiveCrossFilter? = null,
  onClearCrossFilter: () -> Unit = {},
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
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var showOptionsPanel by remember { mutableStateOf(false) }
  var ifaceDropdownExpanded by remember { mutableStateOf(false) }
  val interfaces = listOf("wlan0 (Wi-Fi)", "tun0 (VPN)", "rmnet_data0 (Cellular)", "lo (Loopback)")

  // Stable in-page selected packet state
  var internalSelectedPacket by remember { mutableStateOf<PacketEntity?>(null) }
  var autoScrollEnabled by remember { mutableStateOf(false) }

  val packetListState = rememberLazyListState()

  // Auto-scroll when new packets arrive only if autoScroll is enabled
  LaunchedEffect(recentPackets.size, autoScrollEnabled) {
    if (autoScrollEnabled && recentPackets.isNotEmpty()) {
      packetListState.animateScrollToItem(0)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .verticalScroll(rememberScrollState())
      .testTag("capture_screen")
  ) {
    // Cross filter active banner
    if (activeCrossFilter != null && activeCrossFilter.isActive) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 10.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Filtered by: ${activeCrossFilter.toDisplaySummary()}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1
            )
          }
          IconButton(onClick = onClearCrossFilter, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Clear Cross Filter", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
    // 1. Capture Status Header Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("capture_status_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
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

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Grid (Packets, Data, Duration, Rate)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          CaptureKpiBox("Packets", "${stats.totalPacketsCaptured}", Modifier.weight(1f))
          CaptureKpiBox("Data Size", "${String.format(Locale.US, "%.2f", stats.totalBytesCaptured / 1024.0 / 1024.0)} MB", Modifier.weight(1f))
          CaptureKpiBox("Duration", formatDuration(stats.durationSeconds), Modifier.weight(1f))
          CaptureKpiBox("Throughput", "${String.format(Locale.US, "%.1f", stats.downloadSpeedMbps + stats.uploadSpeedMbps)} MB/s", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Controls Row (START, STOP, PAUSE, CLEAR, EXPORT)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            onClick = {
              internalSelectedPacket = null
              onClearPackets()
            },
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

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Capture Options & BPF Filters (Expandable)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("capture_options_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
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
          Column(modifier = Modifier.padding(top = 10.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

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

    Spacer(modifier = Modifier.height(14.dp))

    // 3. TOP SECTION: LIVE PACKET STREAM
    Card(
      modifier = Modifier.fillMaxWidth().testTag("live_packets_section_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Section Header with Jump to Latest & Auto-scroll
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Radio,
              contentDescription = null,
              tint = if (isCapturing && !isPaused) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LIVE PACKET STREAM",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "${recentPackets.size}",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Jump to Latest Button
          if (recentPackets.isNotEmpty()) {
            OutlinedButton(
              onClick = {
                coroutineScope.launch {
                  packetListState.animateScrollToItem(0)
                }
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Jump to Latest", fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentPackets.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Radio,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = if (isCapturing) "Listening for packets on $activeInterface..." else "Press START CAPTURE to stream live traffic",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          // Scrollable Live Packets Container
          LazyColumn(
            state = packetListState,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(
              items = recentPackets,
              key = { it.id }
            ) { packet ->
              val isSelected = internalSelectedPacket?.id == packet.id
              LivePacketStreamItem(
                packet = packet,
                isSelected = isSelected,
                onClick = {
                  internalSelectedPacket = packet
                  onPacketClick(packet)
                }
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 4. BOTTOM SECTION: STABLE SELECTED PACKET DETAILS
    if (internalSelectedPacket != null) {
      val packet = internalSelectedPacket!!
      Card(
        modifier = Modifier.fillMaxWidth().testTag("packet_details_stable_panel"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Detail Header with Close / Copy
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "PACKET DETAILS #${packet.id}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = {
                  val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                  val text = "Packet #${packet.id} | ${packet.protocol} | ${packet.sourceIp}:${packet.sourcePort} -> ${packet.destIp}:${packet.destPort} | App: ${packet.appName} | Info: ${packet.info}"
                  cb?.setPrimaryClip(ClipData.newPlainText("Packet Details", text))
                  Toast.makeText(context, "Packet details copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
              }
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(
                onClick = { internalSelectedPacket = null },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Hide Details", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

          // 1. Basic Information Section
          DetailSectionHeader("1. BASIC INFORMATION")
          DetailKeyValueRow("Packet Number", "#${packet.id}")
          DetailKeyValueRow("Timestamp", packet.timeFormatted)
          DetailKeyValueRow("Frame Size", "${packet.length} Bytes")
          DetailKeyValueRow("Direction", if (packet.sourceIp.startsWith("10.") || packet.sourceIp.startsWith("192.168.")) "Outbound" else "Inbound")
          DetailKeyValueRow("Capture Interface", activeInterface)

          Spacer(modifier = Modifier.height(10.dp))

          // 2. Network Layer Section
          DetailSectionHeader("2. NETWORK LAYER (IP)")
          DetailKeyValueRow("Source IP", packet.sourceIp)
          DetailKeyValueRow("Destination IP", packet.destIp)
          DetailKeyValueRow("IP Version / Protocol", "${if (packet.sourceIp.contains(":")) "IPv6" else "IPv4"} / ${packet.protocol}")
          DetailKeyValueRow("Packet Summary", packet.info)

          Spacer(modifier = Modifier.height(10.dp))

          // 3. Transport Layer Section
          DetailSectionHeader("3. TRANSPORT LAYER (${packet.protocol})")
          DetailKeyValueRow("Source Port", "${packet.sourcePort}")
          DetailKeyValueRow("Destination Port", "${packet.destPort} (${getPortServiceDescription(packet.destPort)})")
          DetailKeyValueRow("Transport Type", if (packet.protocol == "TCP") "Transmission Control Protocol (Stateful)" else "User Datagram Protocol (Stateless)")

          Spacer(modifier = Modifier.height(10.dp))

          // 4. Application Layer Section
          DetailSectionHeader("4. APPLICATION LAYER")
          DetailKeyValueRow("Application Name", packet.appName)
          DetailKeyValueRow("Application Package", packet.appPackage)
          DetailKeyValueRow("Host / SNI Domain", packet.host ?: "Direct IP Connection")

          Spacer(modifier = Modifier.height(10.dp))

          // 5. Raw Payload Data Preview (Hex & ASCII)
          DetailSectionHeader("5. PAYLOAD FORENSICS (HEX / ASCII)")
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = generateSampleHexPayload(packet),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Close / Hide button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(
              onClick = { internalSelectedPacket = null }
            ) {
              Text("Hide Details", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      // Empty Selection Card Placeholder
      Card(
        modifier = Modifier.fillMaxWidth().testTag("packet_details_placeholder"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Tap any packet in the Live Stream above to inspect full headers, transport layers, and raw payload data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun LivePacketStreamItem(
  packet: PacketEntity,
  isSelected: Boolean,
  onClick: () -> Unit
) {
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
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("packet_row_${packet.id}"),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(8.dp),
    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Protocol Badge
        Surface(
          color = protoColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = packet.protocol,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = protoColor,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            fontSize = 10.sp
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "#${packet.id}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              fontSize = 10.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${packet.sourceIp}:${packet.sourcePort} → ${packet.destIp}:${packet.destPort}",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
          }
          Text(
            text = "${packet.appName} • ${packet.length} B • ${packet.timeFormatted}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            maxLines = 1
          )
        }
      }

      Text(
        text = "${packet.length} B",
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
private fun DetailSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    fontSize = 10.5.sp
  )
  Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun DetailKeyValueRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 11.sp
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 11.sp,
      maxLines = 1
    )
  }
}

private fun getPortServiceDescription(port: Int): String {
  return when (port) {
    80 -> "HTTP Web"
    443 -> "HTTPS / TLS Encrypted"
    53 -> "DNS Resolution"
    8080 -> "HTTP Alternate"
    8443 -> "HTTPS Alternate"
    22 -> "SSH Secure Shell"
    123 -> "NTP Network Time"
    853 -> "DNS over TLS"
    else -> "Application Port $port"
  }
}

private fun generateSampleHexPayload(packet: PacketEntity): String {
  val hexHeader = "0000  45 00 00 ${String.format("%02x", (packet.length.coerceIn(40, 1500) % 256))} 1a 2b 40 00 40 06 c3 d4  E..@.@.."
  val hexTransport = "\n0010  0a 01 0a 01 ${String.format("%02x %02x", (packet.sourcePort / 256), (packet.sourcePort % 256))} ${String.format("%02x %02x", (packet.destPort / 256), (packet.destPort % 256))} 00 00 00 00  ........"
  val hexData = "\n0020  16 03 03 00 4f 01 00 00 4b 03 03 89 2a c1 f2 90  ....O...K...*..."
  return hexHeader + hexTransport + hexData
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

private fun formatDuration(seconds: Long): String {
  val h = seconds / 3600
  val m = (seconds % 3600) / 60
  val s = seconds % 60
  return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
  else String.format(Locale.US, "%02d:%02d", m, s)
}

