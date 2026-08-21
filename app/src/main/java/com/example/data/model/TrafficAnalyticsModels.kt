package com.example.data.model

enum class GlobalTimeRange(val label: String, val shortLabel: String, val durationMs: Long) {
  LAST_5_MINUTES("Last 5 Minutes", "5m", 5 * 60 * 1000L),
  LAST_15_MINUTES("Last 15 Minutes", "15m", 15 * 60 * 1000L),
  LAST_1_HOUR("Last 1 Hour", "1h", 60 * 60 * 1000L),
  LAST_6_HOURS("Last 6 Hours", "6h", 6 * 60 * 60 * 1000L),
  LAST_24_HOURS("Last 24 Hours", "24h", 24 * 60 * 60 * 1000L),
  ALL_TIME("All Time", "All", Long.MAX_VALUE)
}

data class ActiveCrossFilter(
  val appName: String? = null,
  val appPackage: String? = null,
  val ipAddress: String? = null,
  val protocol: String? = null,
  val port: Int? = null,
  val customExpression: String? = null
) {
  val isActive: Boolean
    get() = appName != null || ipAddress != null || protocol != null || port != null || !customExpression.isNullOrBlank()

  fun toDisplaySummary(): String {
    val parts = mutableListOf<String>()
    appName?.let { parts.add("App: $it") }
    ipAddress?.let { parts.add("IP: $it") }
    protocol?.let { parts.add("Proto: $it") }
    port?.let { parts.add("Port: $it") }
    customExpression?.let { if (it.isNotBlank()) parts.add("Expr: $it") }
    return parts.joinToString(" • ")
  }
}

data class EnhancedProtocolAnalysis(
  val protocol: String,
  val packetCount: Int,
  val totalBytes: Long,
  val bytePercentage: Float,
  val packetPercentage: Float,
  val activeFlows: Int,
  val errorCount: Int,
  val errorRatePercent: Float,
  val avgPacketSize: Int,
  val topPorts: List<Int>,
  val layer: String = "Transport", // "Network", "Transport", "Application"
  val isEncrypted: Boolean = false,
  val topApps: List<String> = emptyList()
)

data class ProtocolHierarchyNode(
  val name: String,
  val layer: String,
  val packetCount: Int,
  val byteCount: Long,
  val percentageOfTotal: Float,
  val children: List<ProtocolHierarchyNode> = emptyList()
)

enum class IpThreatRisk(val label: String, val score: Int) {
  CLEAN("Clean / Trusted", 0),
  LOW_RISK("Low Risk / Standard CDN", 15),
  MODERATE_RISK("Moderate / High Volume", 45),
  SUSPICIOUS("Suspicious / Unknown ASN", 75),
  MALICIOUS("Malicious / Threat Intelligence Match", 95)
}

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
  val countryCode: String = "US",
  val city: String = "Ashburn",
  val region: String = "Virginia",
  val asn: String = "AS15169 Google LLC",
  val isp: String = "Google Cloud Platform",
  val organization: String = "Google Infrastructure",
  val threatRisk: IpThreatRisk = IpThreatRisk.CLEAN,
  val threatNotes: String = "Verified institutional Cloud & CDN endpoint",
  val isLocal: Boolean = false,
  val firstSeen: Long = System.currentTimeMillis() - 3600000L,
  val lastSeen: Long = System.currentTimeMillis(),
  val estimatedRttMs: Double = 18.5
)

enum class SocketConnectionState(val label: String) {
  ESTABLISHED("ESTABLISHED"),
  SYN_SENT("SYN_SENT"),
  TIME_WAIT("TIME_WAIT"),
  LISTEN("LISTEN"),
  CLOSE_WAIT("CLOSE_WAIT"),
  CLOSED("CLOSED")
}

data class EnhancedSocketConnection(
  val connectionId: String,
  val appName: String,
  val appPackage: String,
  val localIp: String,
  val localPort: Int,
  val remoteIp: String,
  val remotePort: Int,
  val remoteHostname: String,
  val protocol: String,
  val state: SocketConnectionState,
  val totalBytes: Long,
  val uploadBytes: Long,
  val downloadBytes: Long,
  val packetCount: Int,
  val rttMs: Double,
  val durationSeconds: Double,
  val isEncryptedTls: Boolean = true,
  val processUid: Int = 10042,
  val startTimeFormatted: String = "Active"
)

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

enum class AlertCategory(val displayName: String) {
  HIGH_APP_TRAFFIC("High App Traffic"),
  HIGH_IP_TRAFFIC("High Host Traffic"),
  TRAFFIC_SPIKE("Bandwidth Spike"),
  LARGE_UPLOAD("Large Data Upload"),
  LARGE_DOWNLOAD("Large Data Download"),
  QUOTA_EXCEEDED("App Quota Exceeded"),
  UNKNOWN_TRAFFIC("Unclassified Endpoint"),
  NEW_REMOTE_IP("New External IP"),
  UNUSUAL_PROTOCOL("Suspicious Protocol"),
  CONNECTION_SPIKE("Socket Flow Spike"),
  CLEARTEXT_HTTP_EXPOSURE("Cleartext HTTP"),
  PORT_SCAN_RECONNAISSANCE("Port Scan Recon"),
  SUSPICIOUS_DNS_QUERY("DNS Query Spike")
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
  val percentageOfThreshold: Float,
  val targetIp: String? = null,
  val targetApp: String? = null,
  val targetProtocol: String? = null,
  val recommendedMitigation: String = "Inspect connection flows and restrict background data transfer"
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

