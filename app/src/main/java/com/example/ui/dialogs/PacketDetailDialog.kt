package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.PacketEntity

@Composable
fun PacketDetailDialog(
  packet: PacketEntity,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("packet_detail_dialog"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Title Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Packet #${packet.id} Detail",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${packet.appName} (${packet.protocol})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_packet_detail_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabs: Protocol Tree vs Hex Dump
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Protocol Tree") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Hex / ASCII") }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
          // Protocol Tree View
          ProtocolTreeItem(title = "Frame ${packet.id}", detail = "${packet.length} bytes captured at ${packet.timeFormatted}")
          ProtocolTreeItem(title = "Ethernet II", detail = "Src: 12:34:56:78:9A:BC, Dst: FE:DC:BA:98:76:54")
          ProtocolTreeItem(
            title = "Internet Protocol Version 4",
            detail = "Source: ${packet.sourceIp}, Dest: ${packet.destIp}, Protocol: ${packet.protocol}"
          )
          ProtocolTreeItem(
            title = "Transport Layer (${packet.protocol})",
            detail = "Src Port: ${packet.sourcePort}, Dst Port: ${packet.destPort}, Win=65535"
          )

          if (packet.protocol == "TLS" || packet.isEncrypted) {
            ProtocolTreeItem(
              title = "Transport Layer Security (TLS)",
              detail = "SNI: ${packet.tlsSni ?: packet.host}\nCipher: ${packet.tlsCipherSuite ?: "TLS_AES_256_GCM_SHA384"}"
            )
          }

          if (packet.protocol == "HTTP" || packet.isDecryptedHttp) {
            ProtocolTreeItem(
              title = "Hypertext Transfer Protocol (HTTP)",
              detail = "Method: ${packet.httpMethod ?: "GET"}\nURL: ${packet.httpUrl ?: "https://${packet.host}"}\nStatus: ${packet.httpStatusCode ?: 200}"
            )
          }
        } else {
          // Hex Dump View
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 240.dp)
              .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
              .padding(12.dp)
              .horizontalScroll(rememberScrollState())
          ) {
            Column {
              Text(
                text = "OFFSET   00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F  ASCII",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.Bold
              )
              HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF334155))

              val bytes = packet.payloadHex.split(" ")
              val chunks = bytes.chunked(16)
              chunks.forEachIndexed { index, chunk ->
                val offset = String.format("%04X", index * 16)
                val hexFirst = chunk.take(8).joinToString(" ")
                val hexSecond = if (chunk.size > 8) chunk.drop(8).joinToString(" ") else ""
                val ascii = packet.payloadAscii.take(16)

                Text(
                  text = "$offset   ${hexFirst.padEnd(23)}  ${hexSecond.padEnd(23)}  |$ascii|",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  color = Color(0xFFF8FAFC)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss_packet_detail_button")
          ) {
            Text("Close")
          }
        }
      }
    }
  }
}

@Composable
private fun ProtocolTreeItem(title: String, detail: String) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = "▶ $title",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
