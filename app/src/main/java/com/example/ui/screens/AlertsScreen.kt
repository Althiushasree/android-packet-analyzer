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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmSeverity
import com.example.data.model.TrafficAlertItem
import com.example.util.CsvExportUtils
import java.util.Locale

@Composable
fun AlertsScreen(
  alerts: List<TrafficAlertItem>,
  selectedSeverity: String,
  onSelectSeverity: (String) -> Unit,
  onAlertClick: (TrafficAlertItem) -> Unit
) {
  val context = LocalContext.current
  val severities = listOf("ALL", "CRITICAL", "HIGH", "WARNING", "MONITOR", "NORMAL")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .testTag("alerts_screen")
  ) {
    // Header & Export
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Traffic Alerts & Security Alarms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("${alerts.size} active security triggers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(
        onClick = {
          val csv = CsvExportUtils.exportAlertsToCsv(alerts)
          CsvExportUtils.shareCsv(context, csv, "Export Security Alerts CSV")
        }
      ) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Severity Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(severities) { sev ->
        FilterChip(
          selected = selectedSeverity == sev,
          onClick = { onSelectSeverity(sev) },
          label = { Text(sev, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (alerts.isEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No active traffic alarms or threshold violations", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(items = alerts, key = { it.id }) { alert ->
          AlertCardItem(alert = alert, onClick = { onAlertClick(alert) })
        }
      }
    }
  }
}

@Composable
private fun AlertCardItem(alert: TrafficAlertItem, onClick: () -> Unit) {
  val severityColor = when (alert.severity) {
    AlarmSeverity.CRITICAL -> Color(0xFFDC2626)
    AlarmSeverity.HIGH -> Color(0xFFEA580C)
    AlarmSeverity.WARNING -> Color(0xFFF59E0B)
    AlarmSeverity.MONITOR -> Color(0xFF2563EB)
    AlarmSeverity.NORMAL, AlarmSeverity.INFO -> Color(0xFF16A34A)
  }

  Card(
    modifier = Modifier.fillMaxWidth().clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
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
              .size(10.dp)
              .clip(CircleShape)
              .background(severityColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(alert.entityName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        Surface(
          color = severityColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = alert.severity.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = severityColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = alert.reason,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Progress bar if threshold is defined
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${alert.currentTrafficFormatted} / ${alert.thresholdFormatted}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = alert.timeFormatted,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      LinearProgressIndicator(
        progress = { (alert.percentageOfThreshold / 100f).coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        color = severityColor,
        trackColor = severityColor.copy(alpha = 0.15f)
      )
    }
  }
}
