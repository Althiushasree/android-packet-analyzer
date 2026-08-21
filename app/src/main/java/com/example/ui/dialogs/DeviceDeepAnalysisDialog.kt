package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.intelligence.DeviceType
import com.example.data.intelligence.ObservedNetworkDevice
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceDeepAnalysisDialog(
  device: ObservedNetworkDevice,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 16.dp)
        .testTag("device_deep_analysis_dialog"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = when (device.estimatedDeviceType) {
                    DeviceType.ROUTER, DeviceType.GATEWAY, DeviceType.ACCESS_POINT -> Icons.Default.Router
                    DeviceType.SMARTPHONE -> Icons.Default.PhoneAndroid
                    DeviceType.LAPTOP -> Icons.Default.Laptop
                    DeviceType.PRINTER -> Icons.Default.Print
                    DeviceType.IOT_DEVICE -> Icons.Default.Sensors
                    else -> Icons.Default.Computer
                  },
                  contentDescription = "Device Icon",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "DEVICE DEEP ANALYSIS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
              )
              Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_device_deep_analysis")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

        // Identification & Hardware Attributes
        Text(
          text = "Device Identification",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailRow("IP Address", device.ipAddress, isMono = true)
            DetailRow("MAC Address", device.macAddress, isMono = true)
            DetailRow("Hostname", device.hostname)
            DetailRow("Vendor", device.vendor)
            DetailRow("Estimated Device Type", device.estimatedDeviceType.name.replace("_", " "))
            DetailRow("Discovery Source", device.confidence)
            DetailRow("Status", if (device.isActive) "Active (Responding to Traffic/Probes)" else "Inactive (Retaining Historical Metrics)")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Traffic & Timeline Metrics
        Text(
          text = "Observed Traffic & Timeline",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          TrafficCard(
            modifier = Modifier.weight(1f),
            title = "First Seen",
            value = device.firstSeenFormatted.ifEmpty { "Active" },
            icon = Icons.Default.Info,
            tint = MaterialTheme.colorScheme.secondary
          )
          TrafficCard(
            modifier = Modifier.weight(1f),
            title = "Last Seen",
            value = device.lastSeenFormatted.ifEmpty { "Just Now" },
            icon = Icons.Default.CheckCircle,
            tint = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          TrafficCard(
            modifier = Modifier.weight(1f),
            title = "Total Upload",
            value = formatBytes(device.uploadBytes),
            icon = Icons.Default.ArrowUpward,
            tint = MaterialTheme.colorScheme.tertiary
          )
          TrafficCard(
            modifier = Modifier.weight(1f),
            title = "Total Download",
            value = formatBytes(device.downloadBytes.coerceAtLeast(device.totalBytes)),
            icon = Icons.Default.ArrowDownward,
            tint = MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protocols & Open Ports
        Text(
          text = "Protocols & Observable Ports",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Observed Protocols", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              val protos = if (device.observedProtocols.isNotEmpty()) device.observedProtocols else listOf("TCP", "UDP", "TLS", "DNS")
              protos.forEach { proto ->
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = proto,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Observed Open Ports", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            if (device.openPorts.isNotEmpty()) {
              FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                device.openPorts.forEach { port ->
                  Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = "Port $port",
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                  }
                }
              }
            } else {
              Text(
                text = "No open listening service ports actively responsive to subnet probes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Ownership Notice
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Owner identity: Unknown (Device identifies network interface only, respecting user privacy controls).",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String, isMono: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      text = value.ifEmpty { "Not observable" },
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold,
      fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun TrafficCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
    }
  }
}

private fun formatBytes(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val kb = bytes / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  return when {
    gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
    mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
    kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
    else -> "$bytes B"
  }
}
