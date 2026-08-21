package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.util.NetworkAnalyticsConfig
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Exact palette from user's design screenshot:
// 1. Blue #2979FF (142.250.190.46 / YouTube)
// 2. Green #00C853 (157.240.241.35 / Chrome)
// 3. Purple #7C4DFF (104.244.42.1 / Instagram)
// 4. Orange #FF6D00 (172.217.14.206 / Spotify)
// 5. Pink #F50057 (8.8.8.8 / WhatsApp)
// 6. Slate Grey #78909C (Other / Other Apps)
val ChartPalette = listOf(
  Color(0xFF2979FF), // Vibrant Blue
  Color(0xFF00C853), // Vibrant Green
  Color(0xFF7C4DFF), // Vibrant Purple
  Color(0xFFFF6D00), // Vibrant Orange
  Color(0xFFF50057), // Vibrant Pink / Magenta
  Color(0xFF78909C)  // Slate Grey for Other
)

data class OtherItemDetail(
  val id: String,
  val label: String,
  val secondaryLabel: String? = null,
  val value: Long,
  val downloadBytes: Long = 0L,
  val uploadBytes: Long = 0L,
  val packetCount: Int = 0,
  val percentage: Float
)

data class DonutSegment(
  val id: String,
  val label: String,
  val secondaryLabel: String? = null,
  val value: Long,
  val downloadBytes: Long = 0L,
  val uploadBytes: Long = 0L,
  val packetCount: Int = 0,
  val percentage: Float,
  val color: Color,
  val isOther: Boolean = false,
  val otherItems: List<OtherItemDetail> = emptyList()
)

@Composable
fun InteractiveDonutChart(
  segments: List<DonutSegment>,
  centerTitle: String = "TOTAL APP DATA",
  centerValueFormatted: String,
  totalDownloadBytes: Long = 0L,
  totalUploadBytes: Long = 0L,
  totalPackets: Int = 0,
  liveThroughputFormatted: String = "2.4 KB/s",
  lastUpdatedFormatted: String? = null,
  selectedSegmentId: String?,
  onSelectSegment: (DonutSegment) -> Unit,
  onInspectDetail: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var showOtherDetailsDialog by remember { mutableStateOf(false) }
  var otherDialogSegment by remember { mutableStateOf<DonutSegment?>(null) }

  val isIpMode = centerTitle.contains("IP", ignoreCase = true)
  val cardTitle = if (isIpMode) "Endpoint Traffic (IPs)" else "Application Traffic"
  val bannerText = if (isIpMode) "Tap any IP or slice to inspect endpoint" else "Tap any app or slice to inspect application"
  val centerHeader = if (isIpMode) "TOTAL IP DATA" else "TOTAL APP DATA"

  val handleSegmentSelection: (DonutSegment) -> Unit = { seg ->
    onSelectSegment(seg)
    if (seg.isOther) {
      otherDialogSegment = seg
      showOtherDetailsDialog = true
    } else {
      onInspectDetail?.invoke(seg.id)
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("interactive_donut_card"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // 1. Header Title
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = cardTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = centerValueFormatted,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (segments.isEmpty()) {
        // Compact Mobile Empty State
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("donut_chart_empty"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.Apps,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isIpMode) "No endpoint traffic detected" else "No application traffic detected",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Start capture to generate a traffic report.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        val totalPercentage = segments.sumOf { it.percentage.toDouble() }.toFloat().coerceAtLeast(0.01f)
        val animatedProgress by animateFloatAsState(
          targetValue = 1.0f,
          animationSpec = tween(
            durationMillis = NetworkAnalyticsConfig.CHART_ANIMATION_DURATION_MS,
            easing = LinearOutSlowInEasing
          ),
          label = "donut_chart_progress"
        )

        // 2. Responsive Mobile Layout: Donut on Top (compact ~180dp), List below
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val isWide = maxWidth >= 540.dp
          if (isWide) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              DonutChartCanvas(
                segments = segments,
                totalPercentage = totalPercentage,
                animationProgress = animatedProgress,
                centerHeader = centerHeader,
                centerValueFormatted = centerValueFormatted,
                selectedSegmentId = selectedSegmentId,
                onSelectSegment = handleSegmentSelection,
                modifier = Modifier.size(190.dp)
              )

              DonutLegendList(
                segments = segments,
                isIpMode = isIpMode,
                selectedSegmentId = selectedSegmentId,
                onSelectSegment = handleSegmentSelection,
                modifier = Modifier.weight(1f)
              )
            }
          } else {
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Centered Mobile Donut Chart
              DonutChartCanvas(
                segments = segments,
                totalPercentage = totalPercentage,
                animationProgress = animatedProgress,
                centerHeader = centerHeader,
                centerValueFormatted = centerValueFormatted,
                selectedSegmentId = selectedSegmentId,
                onSelectSegment = handleSegmentSelection,
                modifier = Modifier
                  .size(180.dp)
                  .padding(vertical = 4.dp)
              )

              Spacer(modifier = Modifier.height(14.dp))

              // Compact Application / IP List Items Below
              DonutLegendList(
                segments = segments,
                isIpMode = isIpMode,
                selectedSegmentId = selectedSegmentId,
                onSelectSegment = handleSegmentSelection,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Compact Info Footer
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = bannerText,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }

  // "Other Traffic Details" Modal Dialog
  if (showOtherDetailsDialog && otherDialogSegment != null) {
    val seg = otherDialogSegment!!
    Dialog(onDismissRequest = { showOtherDetailsDialog = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .testTag("other_traffic_dialog"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(seg.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = seg.color, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (isIpMode) "Other IP Endpoints" else "Other Applications",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Text(
                  text = "Total: ${formatDonutBytes(seg.value)} (${String.format(Locale.US, "%.1f", seg.percentage)}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF64748B)
                )
              }
            }
            IconButton(onClick = { showOtherDetailsDialog = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))
          Spacer(modifier = Modifier.height(8.dp))

          if (seg.otherItems.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No additional hidden consumer items.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .height((seg.otherItems.size * 54).coerceIn(120, 280).dp)
            ) {
              items(seg.otherItems) { item ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      showOtherDetailsDialog = false
                      onInspectDetail?.invoke(item.id)
                    }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isIpMode) {
                      IpNetworkIcon(color = seg.color, isOther = false)
                    } else {
                      AppBrandIcon(label = item.label, isOther = false)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = formatDonutBytes(item.value),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2979FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    }
                  }

                  Text(
                    text = "${String.format(Locale.US, "%.1f", item.percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { showOtherDetailsDialog = false }) {
              Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF2979FF))
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// 1. DONUT CANVAS WITH CENTER LABELS AND PERCENTAGES INSIDE SEGMENTS
// ---------------------------------------------------------------------------
@Composable
fun DonutChartCanvas(
  segments: List<DonutSegment>,
  totalPercentage: Float,
  animationProgress: Float,
  centerHeader: String,
  centerValueFormatted: String,
  selectedSegmentId: String?,
  onSelectSegment: (DonutSegment) -> Unit,
  modifier: Modifier = Modifier
) {
  val textMeasurer = rememberTextMeasurer()

  Box(
    modifier = modifier.testTag("donut_ring_container"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(segments) {
          detectTapGestures { tapOffset ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val dx = tapOffset.x - center.x
            val dy = tapOffset.y - center.y
            val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
            val radius = size.width / 2f
            val strokePx = 28.dp.toPx()
            val innerRadius = radius - strokePx

            if (dist in (innerRadius * 0.65f)..radius) {
              var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
              if (angle < 0) angle += 360f
              angle = (angle + 90f) % 360f

              var currentAngle = 0f
              for (seg in segments) {
                val sweep = (seg.percentage / totalPercentage) * 360f
                if (angle >= currentAngle && angle <= currentAngle + sweep) {
                  onSelectSegment(seg)
                  break
                }
                currentAngle += sweep
              }
            }
          }
        }
    ) {
      val diameter = size.minDimension
      val stroke = 26.dp.toPx()
      val arcSize = Size(diameter - stroke, diameter - stroke)
      val topLeft = Offset(stroke / 2f, stroke / 2f)
      val centerX = size.width / 2f
      val centerY = size.height / 2f
      val midRadius = (diameter - stroke) / 2f

      var startAngle = -90f

      segments.forEach { segment ->
        val sweepAngle = ((segment.percentage / totalPercentage) * 360f) * animationProgress
        val isSelected = segment.id == selectedSegmentId
        val segStroke = if (isSelected) stroke * 1.2f else stroke

        // Draw Arc Slice
        drawArc(
          color = segment.color,
          startAngle = startAngle,
          sweepAngle = sweepAngle.coerceAtLeast(1.5f),
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = segStroke, cap = StrokeCap.Butt)
        )

        // Draw inside slice white percentage text
        if (sweepAngle >= 14f && animationProgress > 0.75f) {
          val midAngleDeg = startAngle + sweepAngle / 2f
          val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
          val textX = centerX + (midRadius * cos(midAngleRad)).toFloat()
          val textY = centerY + (midRadius * sin(midAngleRad)).toFloat()

          val pctLabel = String.format(Locale.US, "%.1f%%", segment.percentage)
          val fontSize = if (sweepAngle >= 30f) 10.sp else 8.5.sp
          val textLayoutResult = textMeasurer.measure(
            text = pctLabel,
            style = TextStyle(
              color = Color.White,
              fontSize = fontSize,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
          )
          val textOffset = Offset(
            x = textX - textLayoutResult.size.width / 2f,
            y = textY - textLayoutResult.size.height / 2f
          )
          drawText(
            textLayoutResult = textLayoutResult,
            topLeft = textOffset
          )
        }

        startAngle += sweepAngle
      }
    }

    // Inside Center Content (TOTAL IP DATA / TOTAL APP DATA + Amount)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 14.dp)
    ) {
      Text(
        text = centerHeader,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        fontSize = 9.sp,
        textAlign = TextAlign.Center,
        letterSpacing = 0.5.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = centerValueFormatted,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF0F172A),
        fontSize = 16.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

// ---------------------------------------------------------------------------
// 2. DONUT LEGEND LIST (Name, Icon, Data Size, Percentage)
// ---------------------------------------------------------------------------
@Composable
fun DonutLegendList(
  segments: List<DonutSegment>,
  isIpMode: Boolean,
  selectedSegmentId: String?,
  onSelectSegment: (DonutSegment) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.testTag("donut_legend_container"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    segments.forEach { seg ->
      val isSelected = seg.id == selectedSegmentId
      val rowBg = if (isSelected) seg.color.copy(alpha = 0.08f) else Color.Transparent

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .clickable { onSelectSegment(seg) }
          .testTag("donut_legend_item_${seg.id}"),
        color = rowBg,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(1.dp, seg.color.copy(alpha = 0.6f)) else null
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. App or IP Icon
          if (isIpMode) {
            IpNetworkIcon(color = seg.color, isOther = seg.isOther, modifier = Modifier.size(32.dp))
          } else {
            AppBrandIcon(label = seg.label, isOther = seg.isOther, modifier = Modifier.size(32.dp))
          }

          Spacer(modifier = Modifier.width(10.dp))

          // 2. Center Column: Name, Volume, and Proportional Progress Bar
          Column(
            modifier = Modifier.weight(1f)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (seg.isOther && !isIpMode && seg.label == "Other") "Other Apps" else seg.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )

              Spacer(modifier = Modifier.width(6.dp))

              Text(
                text = "${String.format(Locale.US, "%.1f", seg.percentage)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = formatDonutBytes(seg.value),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = seg.color,
              fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Proportional progress bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(seg.color.copy(alpha = 0.15f))
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(fraction = (seg.percentage / 100f).coerceIn(0.02f, 1f))
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(seg.color)
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// 3. BRAND ICONS & NETWORK GLOBE ICONS MATCHING SCREENSHOT EXACTLY
// ---------------------------------------------------------------------------
@Composable
fun IpNetworkIcon(
  color: Color,
  isOther: Boolean,
  modifier: Modifier = Modifier
) {
  val iconSize = 28.dp
  if (isOther) {
    Box(
      modifier = modifier
        .size(iconSize)
        .clip(CircleShape)
        .background(Color(0xFFF1F5F9)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Language,
        contentDescription = "Other IP",
        tint = Color(0xFF64748B),
        modifier = Modifier.size(18.dp)
      )
    }
  } else {
    Box(
      modifier = modifier
        .size(iconSize)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Language,
        contentDescription = "IP",
        tint = color,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
fun AppBrandIcon(
  label: String,
  isOther: Boolean,
  modifier: Modifier = Modifier
) {
  val clean = label.lowercase().trim()
  val iconSize = 28.dp

  when {
    isOther || clean.contains("other") -> {
      // 4-squares slate grid icon
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF64748B)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.GridView,
          contentDescription = "Other Apps",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }
    clean.contains("youtube") -> {
      // Red rounded rect with white play button
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFFFF0000)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(12.dp)) {
          val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.15f)
            lineTo(size.width * 0.85f, size.height * 0.5f)
            lineTo(size.width * 0.25f, size.height * 0.85f)
            close()
          }
          drawPath(path, Color.White)
        }
      }
    }
    clean.contains("chrome") || clean.contains("browser") -> {
      // Official 4-color Chrome icon
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(CircleShape)
          .background(Color.White)
          .border(1.dp, Color(0xFFE2E8F0), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(20.dp)) {
          val r = size.minDimension / 2f
          drawArc(Color(0xFFEA4335), -120f, 120f, true)
          drawArc(Color(0xFF34A853), 0f, 120f, true)
          drawArc(Color(0xFFFBBC05), 120f, 120f, true)
          drawCircle(Color.White, radius = r * 0.55f)
          drawCircle(Color(0xFF4285F4), radius = r * 0.42f)
        }
      }
    }
    clean.contains("instagram") -> {
      // Instagram gradient rounded rect with camera
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(RoundedCornerShape(6.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF833AB4),
                Color(0xFFFD1D1D),
                Color(0xFFFCAF45)
              )
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(16.dp)) {
          val strokeWidth = 1.6.dp.toPx()
          drawRoundRect(
            color = Color.White,
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5.dp.toPx()),
            style = Stroke(width = strokeWidth)
          )
          drawCircle(
            color = Color.White,
            radius = size.width * 0.22f,
            style = Stroke(width = strokeWidth)
          )
          drawCircle(
            color = Color.White,
            radius = 1.2.dp.toPx(),
            center = Offset(size.width * 0.78f, size.height * 0.22f)
          )
        }
      }
    }
    clean.contains("spotify") || clean.contains("music") -> {
      // Spotify green circle with sound waves
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(CircleShape)
          .background(Color(0xFF1DB954)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(16.dp)) {
          val waveColor = Color.White
          val strokeW = 1.6.dp.toPx()
          // Top wave
          drawArc(
            color = waveColor,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.15f, size.height * 0.20f),
            size = Size(size.width * 0.7f, size.height * 0.42f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
          )
          // Mid wave
          drawArc(
            color = waveColor,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.38f),
            size = Size(size.width * 0.5f, size.height * 0.36f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
          )
          // Bottom wave
          drawArc(
            color = waveColor,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(size.width * 0.35f, size.height * 0.56f),
            size = Size(size.width * 0.3f, size.height * 0.28f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
          )
        }
      }
    }
    clean.contains("whatsapp") -> {
      // WhatsApp vibrant green with chat icon
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(CircleShape)
          .background(Color(0xFF25D366)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Public,
          contentDescription = "WhatsApp",
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }
    clean.contains("telegram") -> {
      // Blue circle with paper plane
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(CircleShape)
          .background(Color(0xFF229ED9)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Telegram",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
      }
    }
    clean.contains("x") || clean.contains("twitter") -> {
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "𝕏",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 15.sp,
          textAlign = TextAlign.Center
        )
      }
    }
    else -> {
      Box(
        modifier = modifier
          .size(iconSize)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF2979FF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Apps,
          contentDescription = label,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

fun formatDonutBytes(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
    mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
    kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
    else -> "$bytes B"
  }
}

