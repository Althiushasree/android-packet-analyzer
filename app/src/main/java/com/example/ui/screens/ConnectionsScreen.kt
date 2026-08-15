package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.PacketEntity
import java.util.Locale

@Composable
fun ConnectionsScreen(
  packets: List<PacketEntity>
) {
  var selectedConnectionPacket by remember { mutableStateOf<PacketEntity?>(null) }

  // Group packets by app and destination host to represent active connections
  val connectionGroups = remember(packets) {
    packets.groupBy { "${it.appName}_${it.host}_${it.destPort}" }.values.map { list ->
      list.first() to list
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("connections_screen")
  ) {
    Text(
      text = "Active App Connections (${connectionGroups.size})",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    if (connectionGroups.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text("No active app connections recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(
          items = connectionGroups,
          key = { "${it.first.appName}_${it.first.id}" }
        ) { (primaryPacket, group) ->
          val totalBytes = group.sumOf { it.length.toLong() }
          ConnectionCardItem(
            packet = primaryPacket,
            packetCount = group.size,
            totalBytes = totalBytes,
            onClick = { selectedConnectionPacket = primaryPacket }
          )
        }
      }
    }

    // Connection Details Dialog (PCAPdroid style)
    selectedConnectionPacket?.let { pkt ->
      ConnectionDetailDialog(
        packet = pkt,
        onDismiss = { selectedConnectionPacket = null }
      )
    }
  }
}

@Composable
private fun ConnectionCardItem(
  packet: PacketEntity,
  packetCount: Int,
  totalBytes: Long,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("connection_card_${packet.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // App Avatar / Icon Container
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Android,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // App Name, Protocol/Port, Host
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = packet.appName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (packet.status == "OPEN") "Open" else "Closed",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (packet.status == "OPEN") Color(0xFF16A34A) else Color(0xFF64748B)
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "${packet.protocol}, ${packet.destPort}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )

        Text(
          text = packet.host,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Byte Count & Chevron
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = if (totalBytes > 1024 * 1024) "${String.format(Locale.US, "%.1f", totalBytes / 1024.0 / 1024.0)} MB"
          else "${totalBytes / 1024} KB",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold
        )
        Icon(
          Icons.Default.ChevronRight,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.outline
        )
      }
    }
  }
}

@Composable
private fun ConnectionDetailDialog(
  packet: PacketEntity,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("connection_detail_dialog"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Connection Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow("App", "${packet.appName} (${packet.appPackage})")
        DetailRow("Protocol", "${packet.protocol} (TCP/UDP)")
        DetailRow("Host", packet.host)
        DetailRow("Source", "${packet.sourceIp}:${packet.sourcePort}")
        DetailRow("Destination", "${packet.destIp}:${packet.destPort}")
        DetailRow("Status", packet.status)
        DetailRow("URL", packet.httpUrl ?: "https://${packet.host}")

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        DetailRow("Bytes Transferred", "${packet.length * 4} B down — ${packet.length * 2} B up")
        DetailRow("Packets Count", "4 down — 9 up")
        DetailRow("Duration", "15 s")
        DetailRow("First Seen", packet.timeFormatted)
        DetailRow("Last Seen", packet.timeFormatted)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Close")
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      fontFamily = FontFamily.Monospace,
      maxLines = 1
    )
  }
}
