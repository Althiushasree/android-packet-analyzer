package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PacketEntity
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * An interactive, expandable list component for packet capture logs.
 * Users can tap anywhere on a row to expand/collapse detailed metadata including
 * source/destination IP, payload length, timestamp, protocol details, application info, and hex payload peek.
 */
@Composable
fun ExpandablePacketCaptureLog(
  packets: List<PacketEntity>,
  modifier: Modifier = Modifier,
  onPacketInspect: ((PacketEntity) -> Unit)? = null,
  onFilterIp: ((String) -> Unit)? = null,
  initiallyExpandedFirst: Boolean = false,
  emptyMessage: String = "No captured packets to display"
) {
  val expandedState = remember(packets, initiallyExpandedFirst) {
    mutableStateMapOf<Long, Boolean>().apply {
      if (initiallyExpandedFirst && packets.isNotEmpty()) {
        put(packets.first().id, true)
      }
    }
  }

  if (packets.isEmpty()) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .testTag("expandable_packet_list_empty"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  } else {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .testTag("expandable_packet_capture_log"),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      packets.forEach { packet ->
        val isExpanded = expandedState[packet.id] == true
        ExpandablePacketRowItem(
          packet = packet,
          isExpanded = isExpanded,
          onToggleExpand = {
            expandedState[packet.id] = !isExpanded
          },
          onInspect = onPacketInspect?.let { { it(packet) } },
          onFilterIp = onFilterIp
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandablePacketRowItem(
  packet: PacketEntity,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  modifier: Modifier = Modifier,
  onInspect: (() -> Unit)? = null,
  onFilterIp: ((String) -> Unit)? = null
) {
  val clipboardManager = LocalClipboardManager.current
  val protoColor = getProtocolColor(packet.protocol)
  val chevronRotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    label = "chevron_rotation"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("packet_item_${packet.id}")
      .border(
        width = if (isExpanded) 1.5.dp else 1.dp,
        color = if (isExpanded) protoColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
      ),
    colors = CardDefaults.cardColors(
      containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
    ),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      // 1. COLLAPSED HEADER ROW (Always Visible - Tap to Expand/Collapse)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onToggleExpand)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Protocol Badge, Packet Index & Timestamp
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            Surface(
              color = protoColor,
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.testTag("packet_proto_${packet.id}")
            ) {
              Text(
                text = packet.protocol,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
              text = "#${packet.id}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
              imageVector = Icons.Default.AccessTime,
              contentDescription = null,
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = packet.timeFormatted,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }

          // Encryption, Length & Chevron Toggle Icon
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = if (packet.isEncrypted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.padding(end = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Icon(
                  imageVector = if (packet.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                  contentDescription = if (packet.isEncrypted) "Encrypted" else "Plaintext",
                  tint = if (packet.isEncrypted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${packet.length} B",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 10.sp,
                  color = if (packet.isEncrypted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
              }
            }

            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = CircleShape,
              modifier = Modifier
                .size(24.dp)
                .testTag("packet_expand_toggle_${packet.id}")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.ExpandMore,
                  contentDescription = if (isExpanded) "Collapse Details" else "Expand Details",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Source -> Destination Flow
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = "${packet.sourceIp}:${packet.sourcePort}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = " ➔ ",
              fontSize = 11.sp,
              color = protoColor,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${packet.destIp}:${packet.destPort}",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = protoColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          if (packet.appName.isNotEmpty()) {
            Text(
              text = packet.appName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(start = 4.dp)
            )
          }
        }

        if (packet.info.isNotEmpty()) {
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = packet.info,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = if (isExpanded) 3 else 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // 2. EXPANDABLE DETAILED METADATA SECTION
      if (isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("packet_expanded_details_${packet.id}")
        ) {
          HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Metadata Grid (Source IP, Dest IP, Payload Length, Exact Timestamp)
          Text(
            text = "DETAILED METADATA",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              // Source IP & Port
              MetadataDetailRow(
                icon = Icons.Default.Hub,
                label = "Source IP : Port",
                value = "${packet.sourceIp} : ${packet.sourcePort}",
                onCopy = { clipboardManager.setText(AnnotatedString("${packet.sourceIp}:${packet.sourcePort}")) }
              )

              // Destination IP & Port
              MetadataDetailRow(
                icon = Icons.Default.Dns,
                label = "Destination IP : Port",
                value = "${packet.destIp} : ${packet.destPort}",
                valueColor = protoColor,
                onCopy = { clipboardManager.setText(AnnotatedString("${packet.destIp}:${packet.destPort}")) }
              )

              // Hostname / SNI
              if (packet.host.isNotEmpty()) {
                MetadataDetailRow(
                  icon = Icons.Default.Language,
                  label = "Resolved Host",
                  value = packet.host,
                  onCopy = { clipboardManager.setText(AnnotatedString(packet.host)) }
                )
              }

              // Payload Length
              val formattedLenKb = String.format(Locale.US, "%.2f KB", packet.length / 1024.0)
              MetadataDetailRow(
                icon = Icons.Default.Storage,
                label = "Payload Length",
                value = "${packet.length} Bytes ($formattedLenKb)",
                onCopy = { clipboardManager.setText(AnnotatedString("${packet.length}")) }
              )

              // Full Formatted Timestamp & Epoch
              val exactTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(packet.timestamp))
              MetadataDetailRow(
                icon = Icons.Default.AccessTime,
                label = "Captured Timestamp",
                value = "$exactTime (${packet.timestamp} ms)",
                onCopy = { clipboardManager.setText(AnnotatedString(exactTime)) }
              )

              // Application & Package
              if (packet.appName.isNotEmpty()) {
                MetadataDetailRow(
                  icon = Icons.Default.Apps,
                  label = "Application",
                  value = "${packet.appName} (${packet.appPackage})",
                  onCopy = { clipboardManager.setText(AnnotatedString(packet.appPackage)) }
                )
              }

              // Protocol & Security
              MetadataDetailRow(
                icon = Icons.Default.Security,
                label = "Protocol & Status",
                value = "${packet.protocol} • Status: ${packet.status} • ${if (packet.isEncrypted) "Encrypted" else "Plaintext"}"
              )

              // Protocol-specific details (HTTP / TLS)
              if (packet.httpMethod != null || packet.httpUrl != null) {
                MetadataDetailRow(
                  icon = Icons.Default.Http,
                  label = "HTTP Request",
                  value = "${packet.httpMethod ?: "GET"} ${packet.httpUrl ?: ""} (${packet.httpStatusCode ?: 200})"
                )
              }
              if (packet.tlsSni != null || packet.tlsCipherSuite != null) {
                MetadataDetailRow(
                  icon = Icons.Default.Lock,
                  label = "TLS Handshake",
                  value = "SNI: ${packet.tlsSni ?: "n/a"} • Cipher: ${packet.tlsCipherSuite ?: "TLS_AES_256_GCM"}"
                )
              }
            }
          }

          // Payload Preview (Hex / ASCII snippet)
          if (packet.payloadHex.isNotEmpty() || packet.payloadAscii.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "PAYLOAD SNAPSHOT",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                if (packet.payloadHex.isNotEmpty()) {
                  Text(
                    text = "HEX: " + packet.payloadHex.take(64) + if (packet.payloadHex.length > 64) "..." else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (packet.payloadAscii.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "ASCII: " + packet.payloadAscii.take(64) + if (packet.payloadAscii.length > 64) "..." else "",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Action Buttons Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Copy Full Summary Action
            OutlinedButton(
              onClick = {
                val summary = buildString {
                  appendLine("Packet #${packet.id} [${packet.protocol}]")
                  appendLine("Time: ${packet.timeFormatted} (${packet.timestamp})")
                  appendLine("Source: ${packet.sourceIp}:${packet.sourcePort}")
                  appendLine("Destination: ${packet.destIp}:${packet.destPort}")
                  appendLine("Host: ${packet.host}")
                  appendLine("Length: ${packet.length} bytes")
                  appendLine("App: ${packet.appName} (${packet.appPackage})")
                  appendLine("Info: ${packet.info}")
                }
                clipboardManager.setText(AnnotatedString(summary))
              },
              modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .testTag("copy_packet_button_${packet.id}"),
              shape = RoundedCornerShape(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Copy Data", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Quick Filter by Dest IP
            if (onFilterIp != null && packet.destIp.isNotEmpty()) {
              OutlinedButton(
                onClick = { onFilterIp(packet.destIp) },
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
                  .testTag("filter_ip_button_${packet.id}"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
              ) {
                Icon(Icons.Default.FilterAlt, contentDescription = "Filter", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filter IP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            // Inspect Full Payload / Dialog
            if (onInspect != null) {
              Button(
                onClick = onInspect,
                modifier = Modifier
                  .weight(1.2f)
                  .height(36.dp)
                  .testTag("inspect_packet_button_${packet.id}"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Inspect", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Full Inspector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MetadataDetailRow(
  icon: ImageVector,
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  valueColor: Color = MaterialTheme.colorScheme.onSurface,
  onCopy: (() -> Unit)? = null
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "$label: ",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        color = valueColor,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    if (onCopy != null) {
      IconButton(
        onClick = onCopy,
        modifier = Modifier.size(20.dp)
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy $label",
          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}

fun getProtocolColor(protocol: String): Color {
  return when (protocol.uppercase()) {
    "TCP" -> ProtocolTcp
    "UDP" -> ProtocolUdp
    "DNS" -> ProtocolDns
    "TLS" -> ProtocolTls
    "HTTP" -> ProtocolHttp
    "QUIC" -> ProtocolQuic
    else -> ProtocolOther
  }
}
