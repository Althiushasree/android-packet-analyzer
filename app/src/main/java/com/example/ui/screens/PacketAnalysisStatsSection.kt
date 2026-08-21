package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.HighestTrafficConsumer
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import com.example.ui.components.ChartPalette
import com.example.ui.components.DonutSegment
import com.example.ui.components.HorizontalBarItem
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.InteractiveHorizontalBarChart
import com.example.ui.components.OtherItemDetail
import com.example.ui.components.UsageTimelineChart
import com.example.ui.components.formatDonutBytes
import java.util.Locale

@Composable
fun PacketAnalysisStatsSection(
  highestConsumer: HighestTrafficConsumer,
  topApps: List<DetailedAppTraffic>,
  topIps: List<DetailedIpTraffic>,
  timelineScope: TimelineScope,
  timelinePoints: List<TimelineDataPoint>,
  onScopeChanged: (TimelineScope) -> Unit,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onInspectIp: (DetailedIpTraffic) -> Unit
) {
  var selectedChartMode by remember { mutableIntStateOf(0) } // 0: Top Apps Bar, 1: Top IPs Bar, 2: Apps Donut, 3: IPs Donut
  var selectedSegmentId by remember { mutableStateOf<String?>(null) }

  Column(modifier = Modifier.fillMaxWidth()) {
    // Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Packet & Flow Analysis",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // HIGHEST TRAFFIC SUMMARY CARD (App, IP, Connection, Protocol)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "HIGHEST TRAFFIC CONSUMERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
          )
          if (highestConsumer.topAppBytes > 0L) {
            Text(
              text = "Live Peak",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (highestConsumer.topAppBytes == 0L && topApps.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No traffic captured yet. Start capture to detect consumers.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Top App
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  topApps.firstOrNull()?.let { onInspectApp(it) }
                },
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  "Top Application",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = highestConsumer.topAppName,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = formatDonutBytes(highestConsumer.topAppBytes),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            // Top IP
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  topIps.firstOrNull()?.let { onInspectIp(it) }
                },
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  "Top Endpoint IP",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = highestConsumer.topIp,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = formatDonutBytes(highestConsumer.topIpBytes),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0D9488)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Highest Traffic Connection (Clean multi-line structured layout)
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "TOP TRAFFIC CONNECTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp
                  )
                }
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = formatDonutBytes(highestConsumer.topConnectionBytes),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = highestConsumer.topConnection,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // MULTI-CHART CARD (Horizontal Bar vs Donut Charts for Apps and IPs)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        // Chart Switcher Tabs
        TabRow(
          selectedTabIndex = selectedChartMode,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          contentColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
          listOf("Apps (Bar)", "IPs (Bar)", "Apps (Donut)", "IPs (Donut)").forEachIndexed { index, title ->
            Tab(
              selected = selectedChartMode == index,
              onClick = {
                selectedChartMode = index
                selectedSegmentId = null
              },
              text = {
                Text(
                  text = title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (selectedChartMode == index) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedChartMode) {
          0 -> {
            // Top 10 Applications Horizontal Bar Chart
            val barItems = topApps.take(10).mapIndexed { idx, app ->
              HorizontalBarItem(
                id = app.appPackage,
                title = app.appName,
                subtitle = app.appPackage,
                totalBytes = app.totalBytes,
                downloadBytes = app.downloadBytes,
                uploadBytes = app.uploadBytes,
                packetCount = app.packetCount,
                percentage = app.percentage,
                rankNumber = idx + 1,
                barColor = ChartPalette[idx % ChartPalette.size]
              )
            }
            InteractiveHorizontalBarChart(
              items = barItems,
              selectedItemId = null,
              onSelectItem = { item ->
                topApps.find { it.appPackage == item.id }?.let { onInspectApp(it) }
              },
              emptyMessage = "No application traffic recorded."
            )
          }
          1 -> {
            // Top 10 IP Addresses Horizontal Bar Chart
            val barItems = topIps.take(10).mapIndexed { idx, ip ->
              HorizontalBarItem(
                id = ip.ip,
                title = ip.ip,
                subtitle = ip.hostname,
                totalBytes = ip.totalBytes,
                downloadBytes = ip.downloadBytes,
                uploadBytes = ip.uploadBytes,
                packetCount = ip.packetCount,
                percentage = ip.percentage,
                rankNumber = idx + 1,
                barColor = ChartPalette[(idx + 2) % ChartPalette.size]
              )
            }
            InteractiveHorizontalBarChart(
              items = barItems,
              selectedItemId = null,
              onSelectItem = { item ->
                topIps.find { it.ip == item.id }?.let { onInspectIp(it) }
              },
              emptyMessage = "No IP traffic recorded."
            )
          }
          2 -> {
            // Applications Donut Chart (Top 5 + Dynamic Other)
            val totalBytes = topApps.sumOf { it.totalBytes }
            val totalDl = topApps.sumOf { it.downloadBytes }
            val totalUl = topApps.sumOf { it.uploadBytes }
            val segments = if (topApps.size > 5) {
              val top5 = topApps.take(5)
              val remaining = topApps.drop(5)
              val top5Segments = top5.mapIndexed { idx, app ->
                DonutSegment(
                  id = app.appPackage,
                  label = app.appName,
                  secondaryLabel = app.appPackage,
                  value = app.totalBytes,
                  downloadBytes = app.downloadBytes,
                  uploadBytes = app.uploadBytes,
                  packetCount = app.packetCount,
                  percentage = app.percentage,
                  color = ChartPalette[idx % ChartPalette.size]
                )
              }
              val otherBytes = remaining.sumOf { it.totalBytes }
              val otherDl = remaining.sumOf { it.downloadBytes }
              val otherUl = remaining.sumOf { it.uploadBytes }
              val otherPkts = remaining.sumOf { it.packetCount }
              val otherPct = if (totalBytes > 0L) {
                (otherBytes.toDouble() / totalBytes.toDouble() * 100.0).toFloat()
              } else 0f
              val otherDetails = remaining.map { r ->
                OtherItemDetail(
                  id = r.appPackage,
                  label = r.appName,
                  secondaryLabel = r.appPackage,
                  value = r.totalBytes,
                  downloadBytes = r.downloadBytes,
                  uploadBytes = r.uploadBytes,
                  packetCount = r.packetCount,
                  percentage = r.percentage
                )
              }
              val otherSegment = DonutSegment(
                id = "__other_apps__",
                label = "Other",
                secondaryLabel = "${remaining.size} other apps",
                value = otherBytes,
                downloadBytes = otherDl,
                uploadBytes = otherUl,
                packetCount = otherPkts,
                percentage = otherPct,
                color = ChartPalette[5 % ChartPalette.size],
                isOther = true,
                otherItems = otherDetails
              )
              top5Segments + otherSegment
            } else {
              topApps.mapIndexed { idx, app ->
                DonutSegment(
                  id = app.appPackage,
                  label = app.appName,
                  secondaryLabel = app.appPackage,
                  value = app.totalBytes,
                  downloadBytes = app.downloadBytes,
                  uploadBytes = app.uploadBytes,
                  packetCount = app.packetCount,
                  percentage = app.percentage,
                  color = ChartPalette[idx % ChartPalette.size]
                )
              }
            }

            InteractiveDonutChart(
              segments = segments,
              centerTitle = "TOTAL APP DATA",
              centerValueFormatted = formatDonutBytes(totalBytes),
              totalDownloadBytes = totalDl,
              totalUploadBytes = totalUl,
              totalPackets = topApps.sumOf { it.packetCount },
              selectedSegmentId = selectedSegmentId,
              onSelectSegment = { seg ->
                selectedSegmentId = seg.id
              },
              onInspectDetail = { id ->
                topApps.find { it.appPackage == id }?.let { onInspectApp(it) }
              }
            )
          }
          3 -> {
            // IP Addresses Donut Chart (Top 5 + Dynamic Other)
            val totalBytes = topIps.sumOf { it.totalBytes }
            val totalDl = topIps.sumOf { it.downloadBytes }
            val totalUl = topIps.sumOf { it.uploadBytes }
            val segments = if (topIps.size > 5) {
              val top5 = topIps.take(5)
              val remaining = topIps.drop(5)
              val top5Segments = top5.mapIndexed { idx, ip ->
                DonutSegment(
                  id = ip.ip,
                  label = ip.ip,
                  secondaryLabel = if (ip.hostname.isNotBlank() && ip.hostname != ip.ip) ip.hostname else null,
                  value = ip.totalBytes,
                  downloadBytes = ip.downloadBytes,
                  uploadBytes = ip.uploadBytes,
                  packetCount = ip.packetCount,
                  percentage = ip.percentage,
                  color = ChartPalette[idx % ChartPalette.size]
                )
              }
              val otherBytes = remaining.sumOf { it.totalBytes }
              val otherDl = remaining.sumOf { it.downloadBytes }
              val otherUl = remaining.sumOf { it.uploadBytes }
              val otherPkts = remaining.sumOf { it.packetCount }
              val otherPct = if (totalBytes > 0L) {
                (otherBytes.toDouble() / totalBytes.toDouble() * 100.0).toFloat()
              } else 0f
              val otherDetails = remaining.map { r ->
                OtherItemDetail(
                  id = r.ip,
                  label = r.ip,
                  secondaryLabel = if (r.hostname.isNotBlank() && r.hostname != r.ip) r.hostname else null,
                  value = r.totalBytes,
                  downloadBytes = r.downloadBytes,
                  uploadBytes = r.uploadBytes,
                  packetCount = r.packetCount,
                  percentage = r.percentage
                )
              }
              val otherSegment = DonutSegment(
                id = "__other_ips__",
                label = "Other",
                secondaryLabel = "${remaining.size} other endpoints",
                value = otherBytes,
                downloadBytes = otherDl,
                uploadBytes = otherUl,
                packetCount = otherPkts,
                percentage = otherPct,
                color = ChartPalette[5 % ChartPalette.size],
                isOther = true,
                otherItems = otherDetails
              )
              top5Segments + otherSegment
            } else {
              topIps.mapIndexed { idx, ip ->
                DonutSegment(
                  id = ip.ip,
                  label = ip.ip,
                  secondaryLabel = if (ip.hostname.isNotBlank() && ip.hostname != ip.ip) ip.hostname else null,
                  value = ip.totalBytes,
                  downloadBytes = ip.downloadBytes,
                  uploadBytes = ip.uploadBytes,
                  packetCount = ip.packetCount,
                  percentage = ip.percentage,
                  color = ChartPalette[idx % ChartPalette.size]
                )
              }
            }

            InteractiveDonutChart(
              segments = segments,
              centerTitle = "TOTAL IP DATA",
              centerValueFormatted = formatDonutBytes(totalBytes),
              totalDownloadBytes = totalDl,
              totalUploadBytes = totalUl,
              totalPackets = topIps.sumOf { it.packetCount },
              selectedSegmentId = selectedSegmentId,
              onSelectSegment = { seg ->
                selectedSegmentId = seg.id
              },
              onInspectDetail = { id ->
                topIps.find { it.ip == id }?.let { onInspectIp(it) }
              }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // TIMELINE USAGE HISTORY CARD (Daily, Monthly, Quarterly, Custom)
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(14.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Network Usage Timeline",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "Aggregated History",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        UsageTimelineChart(
          scope = timelineScope,
          dataPoints = timelinePoints,
          onScopeChanged = onScopeChanged
        )
      }
    }
  }
}
