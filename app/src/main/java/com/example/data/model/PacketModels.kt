package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packets")
data class PacketEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val timeFormatted: String,
  val appName: String,
  val appPackage: String,
  val sourceIp: String,
  val sourcePort: Int,
  val destIp: String,
  val destPort: Int,
  val host: String,
  val protocol: String, // TCP, UDP, DNS, TLS, HTTP, QUIC, ICMP
  val length: Int,
  val info: String,
  val status: String = "ACTIVE", // OPEN, CLOSED, ACTIVE, BLOCKED
  val isEncrypted: Boolean = false,
  val isDecryptedHttp: Boolean = false,
  val httpMethod: String? = null,
  val httpUrl: String? = null,
  val httpStatusCode: Int? = null,
  val tlsSni: String? = null,
  val tlsCipherSuite: String? = null,
  val payloadHex: String = "",
  val payloadAscii: String = ""
)

@Entity(tableName = "pcap_files")
data class PcapFileEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fileName: String,
  val fileSizeFormatted: String,
  val fileSizeBytes: Long,
  val packetCount: Int,
  val timestamp: Long = System.currentTimeMillis(),
  val dateFormatted: String,
  val notes: String = ""
)

@Entity(tableName = "notification_settings")
data class NotificationSettingEntity(
  @PrimaryKey val id: Int = 1,
  val bandwidthAlertEnabled: Boolean = true,
  val bandwidthThresholdMbps: Float = 10.0f,
  val untrustedIpAlertEnabled: Boolean = true,
  val dataLimitAlertEnabled: Boolean = true,
  val dataLimitMb: Float = 250.0f,
  val backgroundTrafficAlertEnabled: Boolean = true,
  val alertOnHttpUnencrypted: Boolean = true,
  val alertSound: Boolean = true,
  val alertVibrate: Boolean = true
)

data class NetworkStats(
  val totalPacketsCaptured: Long = 0,
  val totalBytesCaptured: Long = 0,
  val downloadSpeedMbps: Double = 0.0,
  val uploadSpeedMbps: Double = 0.0,
  val durationSeconds: Long = 0,
  val activeConnectionsCount: Int = 0,
  val openSocketsCount: Int = 0,
  val totalAlarmsCount: Int = 0
)

data class ProtocolDistribution(
  val protocol: String,
  val count: Int,
  val bytes: Long,
  val percentage: Float
)

data class AppTrafficSummary(
  val appName: String,
  val appPackage: String,
  val packetCount: Int,
  val bytesTransferred: Long,
  val percentage: Float
)

data class NetworkAlarm(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: Long,
  val timeFormatted: String,
  val severity: AlarmSeverity
)

enum class AlarmSeverity {
  NORMAL, MONITOR, INFO, WARNING, HIGH, CRITICAL
}

data class TargetAppInfo(
  val appName: String,
  val packageName: String,
  val iconResName: String? = null,
  val isSystemApp: Boolean = false,
  val isSelected: Boolean = true
)
