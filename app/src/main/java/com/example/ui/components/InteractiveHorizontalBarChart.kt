package com.example.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.NetworkAnalyticsConfig
import java.util.Locale

data class HorizontalBarItem(
  val id: String,
  val title: String,
  val subtitle: String? = null,
  val totalBytes: Long,
  val downloadBytes: Long,
  val uploadBytes: Long,
  val packetCount: Int,
  val percentage: Float,
  val rankNumber: Int,
  val barColor: Color = Color(0xFF2563EB)
)

@Composable
fun InteractiveHorizontalBarChart(
  items: List<HorizontalBarItem>,
  selectedItemId: String?,
  onSelectItem: (HorizontalBarItem) -> Unit,
  modifier: Modifier = Modifier,
  emptyMessage: String = "No data available."
) {
  if (items.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = emptyMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    return
  }

  val maxTotal = items.maxOfOrNull { it.totalBytes }?.coerceAtLeast(1L) ?: 1L

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items.forEach { item ->
      val isSelected = item.id == selectedItemId
      val rawRatio = (item.totalBytes.toFloat() / maxTotal.toFloat()).coerceIn(0.02f, 1f)
      val animatedProgressRatio by animateFloatAsState(
        targetValue = rawRatio,
        animationSpec = tween(
          durationMillis = NetworkAnalyticsConfig.CHART_ANIMATION_DURATION_MS,
          easing = LinearOutSlowInEasing
        ),
        label = "bar_progress_ratio_${item.id}"
      )

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .clickable { onSelectItem(item) },
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
      ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
          // Top row: Rank, Title, Subtitle, Total Bytes
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (item.rankNumber <= 3) item.barColor else MaterialTheme.colorScheme.surfaceVariant
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#${item.rankNumber}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (item.rankNumber <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              AppBrandIcon(label = item.title, isOther = false, modifier = Modifier.size(28.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                if (!item.subtitle.isNullOrBlank()) {
                  Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = formatDonutBytes(item.totalBytes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", item.percentage)}% (${item.packetCount} pkts)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Proportional Bar with Download vs Upload segments
          val dlRatio = if (item.totalBytes > 0) item.downloadBytes.toFloat() / item.totalBytes.toFloat() else 0.7f
          val ulRatio = 1f - dlRatio

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = animatedProgressRatio)
                .clip(RoundedCornerShape(5.dp))
            ) {
              // Download segment (Blue/Teal)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .weight(dlRatio.coerceAtLeast(0.01f))
                  .background(item.barColor)
              )
              // Upload segment (Orange/Amber)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .weight(ulRatio.coerceAtLeast(0.01f))
                  .background(Color(0xFFEA580C))
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Micro Breakdown stats (Down / Up)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(item.barColor))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "DL: ${formatDonutBytes(item.downloadBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEA580C)))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "UL: ${formatDonutBytes(item.uploadBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }
  }
}
