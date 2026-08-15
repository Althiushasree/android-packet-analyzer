package com.example.data.model

data class DetailedAppTraffic(
  val appName: String,
  val appPackage: String,
  val totalBytes: Long,
  val downloadBytes: Long,
  val uploadBytes: Long,
  val packetCount: Int,
  val percentage: Float,
  val destinationIps: List<IpUsageSummary> = emptyList(),
  val protocols: List<String> = emptyList(),
  val dailyQuotaBytes: Long = 2000 * 1024 * 1024L, // 2GB default
  val monthlyQuotaBytes: Long = 50L * 1024 * 1024 * 1024L, // 50GB
  val isRegulated: Boolean = false,
  val warningThresholdPercent: Int = 80,
  val avgThroughputKbps: Double = 0.0,
  val peakThroughputKbps: Double = 0.0,
  val connectionCount: Int = 0,
  val topProtocol: String = "TLS",
  val topDestIp: String = ""
)

data class DetailedIpTraffic(
  val ip: String,
  val hostname: String,
  val totalBytes: Long,
  val downloadBytes: Long,
  val uploadBytes: Long,
  val packetCount: Int,
  val percentage: Float,
  val communicatingApps: List<AppUsageSummary> = emptyList(),
  val protocols: List<String> = emptyList(),
  val ports: List<Int> = emptyList(),
  val country: String = "Global CDN",
  val isLocal: Boolean = false
)

data class AppUsageSummary(
  val appName: String,
  val appPackage: String,
  val bytes: Long,
  val packetCount: Int,
  val percentage: Float
)

data class IpUsageSummary(
  val ip: String,
  val hostname: String,
  val bytes: Long,
  val packetCount: Int,
  val percentage: Float
)

data class HighestTrafficConsumer(
  val topAppName: String,
  val topAppBytes: Long,
  val topIp: String,
  val topIpHostname: String,
  val topIpBytes: Long,
  val topConnection: String,
  val topConnectionBytes: Long,
  val topProtocol: String,
  val topProtocolBytes: Long
)

enum class TimelineScope {
  LAST_HOUR, DAILY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, MONTHLY, QUARTERLY, CUSTOM
}

enum class MetricType {
  TOTAL, UPLOAD, DOWNLOAD, PACKETS
}

data class TimelineDataPoint(
  val label: String,
  val timestamp: Long,
  val totalBytes: Long,
  val uploadBytes: Long,
  val downloadBytes: Long,
  val packetCount: Long
)

data class ConversationItem(
  val id: String,
  val sourceIp: String,
  val sourcePort: Int,
  val destIp: String,
  val destPort: Int,
  val protocol: String,
  val packetCount: Int,
  val totalBytes: Long,
  val startTimeFormatted: String,
  val durationSeconds: Double,
  val appName: String
)

data class EndpointItem(
  val address: String,
  val type: String, // "IPv4", "IPv6", "MAC"
  val packetCount: Int,
  val totalBytes: Long,
  val sentBytes: Long,
  val receivedBytes: Long,
  val connectionCount: Int,
  val hostname: String
)

data class PacketLengthBucket(
  val rangeLabel: String,
  val minBytes: Int,
  val maxBytes: Int,
  val count: Int,
  val percentage: Float
)

data class IoGraphPoint(
  val timeLabel: String,
  val timestamp: Long,
  val packetsPerSec: Double,
  val bytesPerSec: Double,
  val uploadBytesPerSec: Double,
  val downloadBytesPerSec: Double
)

data class DisplayFilterPreset(
  val name: String,
  val filterExpression: String,
  val description: String,
  val isBuiltIn: Boolean = true
)

enum class AlertCategory {
  HIGH_APP_TRAFFIC,
  HIGH_IP_TRAFFIC,
  TRAFFIC_SPIKE,
  LARGE_UPLOAD,
  LARGE_DOWNLOAD,
  QUOTA_EXCEEDED,
  UNKNOWN_TRAFFIC,
  NEW_REMOTE_IP,
  UNUSUAL_PROTOCOL,
  CONNECTION_SPIKE
}

data class TrafficAlertItem(
  val id: String,
  val timeFormatted: String,
  val timestamp: Long,
  val severity: AlarmSeverity,
  val category: AlertCategory,
  val entityName: String,
  val reason: String,
  val currentTrafficFormatted: String,
  val thresholdFormatted: String,
  val percentageOfThreshold: Float
)

// Diagnostics tools models
data class PingHopResult(
  val seq: Int,
  val host: String,
  val ip: String,
  val bytes: Int,
  val rttMs: Double,
  val ttl: Int,
  val isSuccess: Boolean
)

data class TracerouteHop(
  val hop: Int,
  val ip: String,
  val host: String,
  val rtt1Ms: Double,
  val rtt2Ms: Double,
  val rtt3Ms: Double,
  val isTimeout: Boolean = false
)

data class DnsRecord(
  val type: String, // A, AAAA, CNAME, MX, NS, TXT
  val value: String,
  val ttl: Int
)

data class PortScanResult(
  val port: Int,
  val serviceName: String,
  val isOpen: Boolean,
  val responseTimeMs: Long
)

data class BandwidthTestResult(
  val downloadMbps: Double,
  val uploadMbps: Double,
  val pingMs: Double,
  val jitterMs: Double,
  val isRunning: Boolean = false,
  val progress: Float = 0f
)

