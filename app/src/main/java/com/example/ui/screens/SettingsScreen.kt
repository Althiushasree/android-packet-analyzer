package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.UserSession
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import java.util.Locale

@Composable
fun SettingsScreen(
  userSession: UserSession?,
  onSignOut: () -> Unit,
  notificationSettings: NotificationSettingEntity,
  onSaveNotificationSettings: (NotificationSettingEntity) -> Unit,
  onOpenTargetAppSelector: () -> Unit,
  onOpenSslCertDialog: () -> Unit
) {
  var settingsState by remember(notificationSettings) { mutableStateOf(notificationSettings) }
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .verticalScroll(scrollState)
      .testTag("settings_screen")
  ) {
    // Institutional Google Account Card
    userSession?.let { session ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
          .testTag("user_account_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = session.displayName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = session.email,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
            OutlinedButton(
              onClick = onSignOut,
              modifier = Modifier.testTag("signout_button")
            ) {
              Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sign Out")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Verified Domain: @cutmac.ap.in",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
    Text(
      text = "Notification & Security Settings",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Capture Target Applications Section
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("Target Apps Sniffing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Select apps to capture traffic for", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          OutlinedButton(
            onClick = onOpenTargetAppSelector,
            modifier = Modifier.testTag("open_target_apps_button")
          ) {
            Text("Select")
          }
        }
      }
    }

    // HTTPS SSL Certificate Section
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("HTTPS SSL Certificate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("User CA Root Certificate Status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          OutlinedButton(
            onClick = onOpenSslCertDialog,
            modifier = Modifier.testTag("manage_ssl_cert_button")
          ) {
            Text("Manage")
          }
        }
      }
    }

    // Custom Threshold Notification Settings
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
          Spacer(modifier = Modifier.width(10.dp))
          Text("Custom Alert Notifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bandwidth Alert Switch & Slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Bandwidth Spike Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
          Switch(
            checked = settingsState.bandwidthAlertEnabled,
            onCheckedChange = {
              settingsState = settingsState.copy(bandwidthAlertEnabled = it)
              onSaveNotificationSettings(settingsState)
            },
            modifier = Modifier.testTag("bandwidth_alert_switch")
          )
        }

        if (settingsState.bandwidthAlertEnabled) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Threshold: ${String.format(Locale.US, "%.1f", settingsState.bandwidthThresholdMbps)} Mbps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Slider(
            value = settingsState.bandwidthThresholdMbps,
            onValueChange = {
              settingsState = settingsState.copy(bandwidthThresholdMbps = it)
            },
            onValueChangeFinished = {
              onSaveNotificationSettings(settingsState)
            },
            valueRange = 1f..100f,
            modifier = Modifier.testTag("bandwidth_threshold_slider")
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Untrusted IP Alert Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Suspicious Outbound IP Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Warn on unverified IP addresses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = settingsState.untrustedIpAlertEnabled,
            onCheckedChange = {
              settingsState = settingsState.copy(untrustedIpAlertEnabled = it)
              onSaveNotificationSettings(settingsState)
            },
            modifier = Modifier.testTag("untrusted_ip_alert_switch")
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Cleartext HTTP Warning
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Unencrypted HTTP Warning", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Notify when unencrypted HTTP traffic is sent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Switch(
            checked = settingsState.alertOnHttpUnencrypted,
            onCheckedChange = {
              settingsState = settingsState.copy(alertOnHttpUnencrypted = it)
              onSaveNotificationSettings(settingsState)
            },
            modifier = Modifier.testTag("http_warning_switch")
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Sound & Vibration Toggles
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alert Sound", style = MaterialTheme.typography.bodyMedium)
          }
          Switch(
            checked = settingsState.alertSound,
            onCheckedChange = {
              settingsState = settingsState.copy(alertSound = it)
              onSaveNotificationSettings(settingsState)
            },
            modifier = Modifier.testTag("alert_sound_switch")
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Vibration", style = MaterialTheme.typography.bodyMedium)
          }
          Switch(
            checked = settingsState.alertVibrate,
            onCheckedChange = {
              settingsState = settingsState.copy(alertVibrate = it)
              onSaveNotificationSettings(settingsState)
            },
            modifier = Modifier.testTag("alert_vibrate_switch")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
