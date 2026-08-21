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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AlarmSeverity
import com.example.data.model.AlertCategory
import com.example.data.model.TrafficAlertItem
import com.example.util.CsvExportUtils
import java.util.Locale

@Composable
fun AlertsScreen(
  alerts: List<TrafficAlertItem>,
  selectedSeverity: String,
  onSelectSeverity: (String) -> Unit,
  onAlertClick: (TrafficAlertItem) -> Unit,
  onInvestigateTarget: (ip: String?, app: String?, protocol: String?) -> Unit = { _, _, _ -> }
) {
  val context = LocalContext.current
  val severities = listOf("ALL", "CRITICAL", "HIGH", "WARNING", "MONITOR", "NORMAL")
  var activeAlertModal by remember { mutableStateOf<TrafficAlertItem?>(null) }

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
        Text("Security Alert Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("${alerts.size} Active Threat & Bandwidth Alerts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    Spacer(modifier = Modifier.height(10.dp))

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

    Spacer(modifier = Modifier.height(10.dp))

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
          AlertCardItem(
            alert = alert,
            onClick = {
              onAlertClick(alert)
              activeAlertModal = alert
            }
          )
        }
      }
    }

    // Modal Dialog for Alert Detail & Action Plan
    activeAlertModal?.let { alert ->
      AlertDetailModalDialog(
        alert = alert,
        onDismiss = { activeAlertModal = null },
        onInvestigate = {
          onInvestigateTarget(alert.targetIp, alert.targetApp, alert.targetProtocol)
          activeAlertModal = null
        }
      )
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
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("alert_card_${alert.id}"),
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
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

      // Category badge & mitigation row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = alert.category.displayName,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp
          )
        }

        Text(
          text = alert.timeFormatted,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

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
          text = "${String.format(Locale.US, "%.0f", alert.percentageOfThreshold)}%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = severityColor
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      LinearProgressIndicator(
        progress = { (alert.percentageOfThreshold / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = severityColor,
        trackColor = severityColor.copy(alpha = 0.15f)
      )
    }
  }
}

@Composable
private fun AlertDetailModalDialog(
  alert: TrafficAlertItem,
  onDismiss: () -> Unit,
  onInvestigate: () -> Unit
) {
  val severityColor = when (alert.severity) {
    AlarmSeverity.CRITICAL -> Color(0xFFDC2626)
    AlarmSeverity.HIGH -> Color(0xFFEA580C)
    AlarmSeverity.WARNING -> Color(0xFFF59E0B)
    AlarmSeverity.MONITOR -> Color(0xFF2563EB)
    AlarmSeverity.NORMAL, AlarmSeverity.INFO -> Color(0xFF16A34A)
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .testTag("alert_detail_modal"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = severityColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alert Intelligence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(alert.entityName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(alert.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(10.dp))

        // Target Metadata
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(8.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            alert.targetApp?.let {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target App:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
            alert.targetIp?.let {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Target Host IP:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
            alert.targetProtocol?.let {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Protocol:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Detection Time:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(alert.timeFormatted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mitigation Pill
        alert.recommendedMitigation?.let { mitigation ->
          Surface(
            color = severityColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = severityColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recommended Mitigation", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = severityColor)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(mitigation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Close")
          }
          Button(
            onClick = onInvestigate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Investigate")
          }
        }
      }
    }
  }
}