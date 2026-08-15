package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IoGraphPoint
import com.example.data.model.NetworkStats
import com.example.data.model.PacketLengthBucket
import com.example.data.model.ProtocolDistribution
import com.example.util.CsvExportUtils
import java.util.Locale

@Composable
fun StatisticsScreen(
  stats: NetworkStats,
  protocols: List<ProtocolDistribution>,
  lengthBuckets: List<PacketLengthBucket>,
  ioPoints: List<IoGraphPoint>,
  selectedInterval: String,
  onSelectInterval: (String) -> Unit
) {
  val context = LocalContext.current
  var ioGraphMetric by remember { mutableStateOf("BYTES") } // BYTES or PACKETS
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .verticalScroll(scrollState)
      .testTag("statistics_screen")
  ) {
    // Header & Export
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Network Statistics & I/O", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Deep flow aggregation & packet size profiling", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(
        onClick = {
          val csv = CsvExportUtils.exportProtocolsToCsv(protocols)
          CsvExportUtils.shareCsv(context, csv, "Export Statistics CSV")
        }
      ) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export Statistics", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Summary 4-KPI Grid
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      SummaryKpiCard("TOTAL PACKETS", "${stats.totalPacketsCaptured}", "Captured in session", Modifier.weight(1f))
      SummaryKpiCard("TOTAL DATA", "${String.format(Locale.US, "%.2f", stats.totalBytesCaptured / 1024.0 / 1024.0)} MB", "Payload & headers", Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      val avgPacketSize = if (stats.totalPacketsCaptured > 0) stats.totalBytesCaptured / stats.totalPacketsCaptured else 0L
      SummaryKpiCard("AVG PACKET SIZE", "$avgPacketSize Bytes", "MTU standard 1500", Modifier.weight(1f))
      SummaryKpiCard("AVG THROUGHPUT", "${String.format(Locale.US, "%.1f", stats.downloadSpeedMbps + stats.uploadSpeedMbps)} MB/s", "Live bandwidth rate", Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    // I/O Graph Card (Packets/sec or Bytes/sec over time)
    Card(
      modifier = Modifier.fillMaxWidth().testTag("io_graph_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("I/O Graph", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }

          // Metric Toggle (Bytes vs Packets)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(
              selected = ioGraphMetric == "BYTES",
              onClick = { ioGraphMetric = "BYTES" },
              label = { Text("Bytes/s", fontSize = 10.sp) },
              shape = RoundedCornerShape(6.dp)
            )
            FilterChip(
              selected = ioGraphMetric == "PACKETS",
              onClick = { ioGraphMetric = "PACKETS" },
              label = { Text("Pkts/s", fontSize = 10.sp) },
              shape = RoundedCornerShape(6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interval chips (1s, 10s, 1m, 5m)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Interval:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          listOf("1s", "10s", "1m", "5m").forEach { interval ->
            FilterChip(
              selected = selectedInterval == interval,
              onClick = { onSelectInterval(interval) },
              label = { Text(interval, fontSize = 10.sp) },
              shape = RoundedCornerShape(6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Line Chart
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
          val lineColor = MaterialTheme.colorScheme.primary
          val uploadColor = Color(0xFFD97706)

          Canvas(modifier = Modifier.fillMaxSize()) {
            if (ioPoints.isNotEmpty()) {
              val maxVal = ioPoints.maxOf { if (ioGraphMetric == "BYTES") it.bytesPerSec else it.packetsPerSec }.coerceAtLeast(10.0)
              val stepX = size.width / (ioPoints.size - 1).coerceAtLeast(1)

              val path = Path()
              val upPath = Path()

              ioPoints.forEachIndexed { index, point ->
                val v = if (ioGraphMetric == "BYTES") point.bytesPerSec else point.packetsPerSec
                val upV = if (ioGraphMetric == "BYTES") point.uploadBytesPerSec else (point.packetsPerSec * 0.3)

                val x = index * stepX
                val y = size.height - ((v / maxVal) * size.height * 0.85f).toFloat()
                val upY = size.height - ((upV / maxVal) * size.height * 0.85f).toFloat()

                if (index == 0) {
                  path.moveTo(x, y)
                  upPath.moveTo(x, upY)
                } else {
                  path.lineTo(x, y)
                  upPath.lineTo(x, upY)
                }
              }

              drawPath(path = path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
              drawPath(path = upPath, color = uploadColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Download / Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Upload", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Packet Length Distribution Histogram
    Card(
      modifier = Modifier.fillMaxWidth().testTag("packet_length_distribution_card"),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Packet Length Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        lengthBuckets.forEach { bucket ->
          Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(bucket.rangeLabel, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
              Text(
                "${bucket.count} pkts (${String.format(Locale.US, "%.1f", bucket.percentage)}%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { (bucket.percentage / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.primaryContainer
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun SummaryKpiCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
      Spacer(modifier = Modifier.height(2.dp))
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
  }
}
