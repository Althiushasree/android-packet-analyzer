package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope

/**
 * Interactive timeline chart wrapper delegating to [UsageTimelineChart].
 */
@Composable
fun InteractiveTimelineChart(
  scope: TimelineScope,
  dataPoints: List<TimelineDataPoint>,
  onScopeChanged: (TimelineScope) -> Unit,
  modifier: Modifier = Modifier
) {
  UsageTimelineChart(
    scope = scope,
    dataPoints = dataPoints,
    onScopeChanged = onScopeChanged,
    modifier = modifier
  )
}
