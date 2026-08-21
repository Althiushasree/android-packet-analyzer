package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.util.NetworkAnalyticsConfig
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import java.util.Locale

enum class TimelineViewStyle {
  BAR, TREND_LINE
}

@Composable
fun UsageTimelineChart(
  scope: TimelineScope,
  dataPoints: List<TimelineDataPoint>,
  onScopeChanged: (TimelineScope) -> Unit,
  modifier: Modifier = Modifier,
  onPointSelected: ((TimelineDataPoint) -> Unit)? = null
) {
  var selectedMetric by remember { mutableIntStateOf(0) } // 0: Total, 1: DL, 2: UL, 3: Packets
  var selectedPointIndex by remember { mutableIntStateOf(0) }
  var chartStyle by remember { mutableStateOf(TimelineViewStyle.BAR) }

  // Clamp selection index whenever dataPoints size changes or is empty
  val safePointIndex = remember(dataPoints, selectedPointIndex) {
    if (dataPoints.isEmpty()) 0 else selectedPointIndex.coerceIn(0, dataPoints.lastIndex)
  }

  val activePoint: TimelineDataPoint? = if (dataPoints.isNotEmpty()) {
    dataPoints.getOrNull(safePointIndex) ?: dataPoints.lastOrNull()
  } else null

  val animProgress by animateFloatAsState(
    targetValue = 1.0f,
    animationSpec = tween(
      durationMillis = NetworkAnalyticsConfig.CHART_ANIMATION_DURATION_MS,
      easing = LinearOutSlowInEasing
    ),
    label = "timeline_anim_progress"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("usage_timeline_chart")
  ) {
    // 1. Period Selector Header (Daily, Monthly, Quarterly, Custom) - Horizontally Scrollable
    ScrollableTabRow(
      selectedTabIndex = scope.ordinal,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .testTag("timeline_scope_tab_row")
    ) {
      TimelineScope.values().forEach { tabScope ->
        val isTabSelected = scope == tabScope
        Tab(
          selected = isTabSelected,
          onClick = { onScopeChanged(tabScope) },
          modifier = Modifier.testTag("timeline_scope_tab_${tabScope.name.lowercase()}"),
          text = {
            Text(
              text = when (tabScope) {
                TimelineScope.LAST_HOUR -> "Last Hour"
                TimelineScope.DAILY -> "Daily"
                TimelineScope.YESTERDAY -> "Yesterday"
                TimelineScope.LAST_7_DAYS -> "7 Days"
                TimelineScope.LAST_30_DAYS -> "30 Days"
                TimelineScope.MONTHLY -> "Monthly"
                TimelineScope.QUARTERLY -> "Quarterly"
                TimelineScope.CUSTOM -> "Custom"
              },
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
              maxLines = 1,
              softWrap = false
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Metric Mode Selector & View Style Switcher
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Metric Chips (Total Data, Download, Upload, Packets)
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val metricLabels = listOf("Total", "Download", "Upload", "Packets")
        metricLabels.forEachIndexed { index, label ->
          val isSelected = selectedMetric == index
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedMetric = index }
              .testTag("timeline_metric_chip_${label.lowercase()}"),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Box(
              modifier = Modifier.padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Chart Style Switcher (Bar vs Trend)
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(modifier = Modifier.padding(2.dp)) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(if (chartStyle == TimelineViewStyle.BAR) MaterialTheme.colorScheme.primary else Color.Transparent)
              .clickable { chartStyle = TimelineViewStyle.BAR }
              .testTag("timeline_style_bar"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.BarChart,
              contentDescription = "Bar Chart",
              tint = if (chartStyle == TimelineViewStyle.BAR) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(if (chartStyle == TimelineViewStyle.TREND_LINE) MaterialTheme.colorScheme.primary else Color.Transparent)
              .clickable { chartStyle = TimelineViewStyle.TREND_LINE }
              .testTag("timeline_style_trend"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.ShowChart,
              contentDescription = "Trend Line",
              tint = if (chartStyle == TimelineViewStyle.TREND_LINE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Graceful Empty State Handling
    if (dataPoints.isEmpty()) {
      TimelineEmptyStateView(scope = scope)
      return
    }

    // 4. Point Highlight Inspector Card
    if (activePoint != null) {
      val metricValStr = when (selectedMetric) {
        0 -> formatDonutBytes(activePoint.totalBytes)
        1 -> formatDonutBytes(activePoint.downloadBytes)
        2 -> formatDonutBytes(activePoint.uploadBytes)
        else -> "${activePoint.packetCount} packets"
      }

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("timeline_point_inspector"),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "TIMELINE ENTRY: ${activePoint.label.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = metricValStr,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "DL: ${formatDonutBytes(activePoint.downloadBytes)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "UL: ${formatDonutBytes(activePoint.uploadBytes)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEA580C)
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${activePoint.packetCount} frames",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 5. Visualizer Renderers (Bar vs Trend Line)
    val metricValues = dataPoints.map { dp ->
      when (selectedMetric) {
        0 -> dp.totalBytes.toDouble()
        1 -> dp.downloadBytes.toDouble()
        2 -> dp.uploadBytes.toDouble()
        else -> dp.packetCount.toDouble()
      }
    }
    val rawMax = metricValues.maxOrNull() ?: 0.0
    val maxVal = if (rawMax <= 0.0) 1.0 else rawMax

    if (chartStyle == TimelineViewStyle.BAR) {
      // Interactive Vertical Timeline Column Visualizer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .padding(horizontal = 4.dp)
          .testTag("timeline_bars_container"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        dataPoints.forEachIndexed { index, point ->
          val value = metricValues.getOrElse(index) { 0.0 }
          val ratio = ((value / maxVal).toFloat()).coerceIn(0.04f, 1f) * animProgress
          val isSelected = index == safePointIndex

          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(6.dp))
              .clickable {
                selectedPointIndex = index
                onPointSelected?.invoke(point)
              }
              .padding(horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
          ) {
            // Value Tooltip Pill if Selected
            if (isSelected) {
              Text(
                text = if (selectedMetric == 3) "${point.packetCount}" else formatShortDonutBytes(point.totalBytes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp,
                maxLines = 1
              )
              Spacer(modifier = Modifier.height(2.dp))
            }

            // Stacked Bar Column (DL in Primary, UL in Amber/Orange)
            val totalPktBytes = point.totalBytes.coerceAtLeast(1L)
            val dlFraction = (point.downloadBytes.toFloat() / totalPktBytes.toFloat()).coerceIn(0.1f, 0.9f)
            val ulFraction = 1f - dlFraction

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(ratio)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                  width = if (isSelected) 1.5.dp else 0.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
            ) {
              Column(modifier = Modifier.fillMaxSize()) {
                // Upload part
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .weight(ulFraction)
                    .background(Color(0xFFEA580C).copy(alpha = if (isSelected) 0.9f else 0.6f))
                )
                // Download part
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .weight(dlFraction)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 1f else 0.75f))
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Label Text
            Text(
              text = point.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp,
              maxLines = 1,
              softWrap = false,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    } else {
      // Smooth Trend Spline / Area Canvas Visualizer
      val primaryColor = MaterialTheme.colorScheme.primary
      val secondaryColor = Color(0xFFEA580C)
      val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .testTag("timeline_trend_container")
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          val canvasWidth = size.width
          val canvasHeight = size.height
          val numPoints = dataPoints.size

          if (numPoints > 0) {
            // Draw horizontal reference grid lines
            for (i in 0..3) {
              val y = canvasHeight * (i / 3f)
              drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
              )
            }

            val stepX = if (numPoints > 1) canvasWidth / (numPoints - 1) else canvasWidth / 2f
            val points = metricValues.mapIndexed { idx, v ->
              val normalized = ((v / maxVal).toFloat()).coerceIn(0.02f, 1f) * animProgress
              val x = if (numPoints > 1) idx * stepX else canvasWidth / 2f
              val y = canvasHeight - (normalized * canvasHeight)
              Offset(x, y)
            }

            // Fill Area Path
            val fillPath = Path().apply {
              moveTo(points.first().x, canvasHeight)
              points.forEach { lineTo(it.x, it.y) }
              lineTo(points.last().x, canvasHeight)
              close()
            }

            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f))
              )
            )

            // Stroke Line
            val strokePath = Path().apply {
              points.forEachIndexed { idx, pt ->
                if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
              }
            }

            drawPath(
              path = strokePath,
              color = primaryColor,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Node Dots
            points.forEachIndexed { idx, pt ->
              val isSel = idx == safePointIndex
              drawCircle(
                color = if (isSel) primaryColor else Color.White,
                radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                center = pt
              )
              drawCircle(
                color = primaryColor,
                radius = if (isSel) 6.dp.toPx() else 4.dp.toPx(),
                center = pt,
                style = Stroke(width = 2.dp.toPx())
              )
            }
          }
        }

        // Timeline Labels beneath trend chart
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          dataPoints.forEachIndexed { idx, point ->
            val isSelected = idx == safePointIndex
            Text(
              text = point.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 9.sp,
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.clickable {
                selectedPointIndex = idx
                onPointSelected?.invoke(point)
              }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 6. Timeline Legend (Download vs Upload color indicator)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Download Traffic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
      }
      Spacer(modifier = Modifier.width(16.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEA580C)))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Upload Traffic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
      }
    }
  }
}

@Composable
fun TimelineEmptyStateView(
  scope: TimelineScope,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("timeline_empty_state"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Timeline,
          contentDescription = "No Data",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "No Historical Traffic Recorded",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "No historical network flow logs found for ${scope.name.lowercase().replaceFirstChar { it.uppercase() }} timeframe. Live captured flows will automatically populate this timeline.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
    }
  }
}

fun formatShortDonutBytes(bytes: Long): String {
  if (bytes <= 0) return "0B"
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1.0 -> String.format(Locale.US, "%.1fG", gb)
    mb >= 1.0 -> String.format(Locale.US, "%.0fM", mb)
    kb >= 1.0 -> String.format(Locale.US, "%.0fK", kb)
    else -> "${bytes}B"
  }
}
