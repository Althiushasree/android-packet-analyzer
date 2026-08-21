package com.example.data.intelligence

import java.util.Locale

/**
 * Data models for the Network Intelligence System.
 * Adheres strictly to: NO DEMO DATA — REAL DATA ONLY.
 * Supports OBSERVED / INFERRED / UNKNOWN classifications.
 */

enum class IntelligenceStatus {
  OBSERVED,
  INFERRED,
  UNKNOWN
}

enum class DeviceType {
  LAPTOP,
  DESKTOP,
  SMARTPHONE,
  TABLET,
  ROUTER,
  ACCESS_POINT,
  PRINTER,
  IOT_DEVICE,
  SERVER,
  GATEWAY,
  LOCAL_DEVICE,
  UNKNOWN
}

enum class AnomalySeverity {
  INFORMATIONAL, // 🟢
  LOW,           // 🟡
  MEDIUM,        // 🟠
  HIGH           // 🔴
}

data class RealNetworkInterfaceInfo(
  val interfaceName: String = "Not observable",
  val interfaceType: String = "Not observable", // Wi-Fi, Ethernet, Mobile, VPN, Loopback
  val isUp: Boolean = false,
  val isConnected: Boolean = false,
  val ssid: String = "Not observable on current network",
  val localIpv4: String = "Not observable",
  val localIpv6: String = "Not observable",
  val macAddress: String = "Not observable on current network",
  val subnetMask: String = "Not observable",
  val subnetPrefixLength: Int = 24,
  val defaultGateway: String = "Not observable",
  val dnsServers: List<String> = emptyList(),
  val dhcpServer: String = "Not observable",
  val mtu: Int = 1500,
  val linkSpeedMbps: Int = -1, // -1 if not observable
  val rxBytes: Long = 0L,
  val txBytes: Long = 0L,
  val rxPackets: Long = 0L,
  val txPackets: Long = 0L,
  val connectionDurationSeconds: Long = 0L,
  val isVpnActive: Boolean = false,
  val isWifi: Boolean = false,
  val isCellular: Boolean = false,
  val isEthernet: Boolean = false
)

data class ObservedNetworkDevice(
  val id: String, // IP or MAC
  val ipAddress: String,
  val macAddress: String = "Not observable",
  val hostname: String = "Not observable",
  val vendor: String = "Unknown Vendor",
  val estimatedDeviceType: DeviceType = DeviceType.UNKNOWN,
  val isLocalHost: Boolean = false,
  val isGateway: Boolean = false,
  val firstSeenTimestamp: Long = System.currentTimeMillis(),
  val lastSeenTimestamp: Long = System.currentTimeMillis(),
  val firstSeenFormatted: String = "",
  val lastSeenFormatted: String = "",
  val isActive: Boolean = true,
  val totalPackets: Long = 0L,
  val totalBytes: Long = 0L,
  val uploadBytes: Long = 0L,
  val downloadBytes: Long = 0L,
  val currentPacketsPerSec: Double = 0.0,
  val currentBytesPerSec: Double = 0.0,
  val activeConnectionsCount: Int = 0,
  val openPorts: List<Int> = emptyList(),
  val observedProtocols: List<String> = emptyList(),
  val contactedDestinations: List<String> = emptyList(),
  val recentDnsQueries: List<String> = emptyList(),
  val confidence: String = "Observed via ARP/Socket Discovery"
)

data class CommunicationFlow(
  val id: String,
  val sourceDeviceIp: String,
  val destinationAddress: String,
  val destinationDomain: String = "",
  val protocol: String,
  val port: Int,
  val packetCount: Long,
  val totalBytes: Long,
  val lastSeenTimestamp: Long,
  val lastSeenFormatted: String,
  val status: IntelligenceStatus = IntelligenceStatus.OBSERVED
)

data class ApplicationServiceAnalysis(
  val id: String,
  val serviceName: String,
  val deviceIp: String,
  val status: IntelligenceStatus, // OBSERVED / INFERRED / UNKNOWN
  val evidence: String,
  val trafficBytes: Long,
  val packetCount: Long,
  val portsUsed: List<Int> = emptyList(),
  val protocol: String = "TLS",
  val domainName: String = "",
  val isEncrypted: Boolean = true,
  val explanation: String = ""
)

data class RealDnsLogEntry(
  val id: String,
  val timestamp: Long,
  val timeFormatted: String,
  val deviceIp: String,
  val dnsServer: String,
  val queryDomain: String,
  val queryType: String = "A",
  val responseAnswer: String = "",
  val latencyMs: Long = 0L,
  val isSuccess: Boolean = true,
  val status: IntelligenceStatus = IntelligenceStatus.OBSERVED
)

data class NetworkHealthReport(
  val healthScore: Int = 100, // 0 - 100
  val statusSummary: String = "Calculating...",
  val gatewayLatencyMs: Double = -1.0, // -1 if not available
  val dnsLatencyMs: Double = -1.0,
  val packetLossPercent: Double = 0.0,
  val throughputMbps: Double = 0.0,
  val retransmissionCount: Long = 0L,
  val interfaceErrors: Long = 0L,
  val connectionFailures: Int = 0,
  val stabilityLevel: String = "Optimal",
  val measurementTimestamp: Long = System.currentTimeMillis()
)

data class DefensiveSecurityAlert(
  val id: String,
  val severity: AnomalySeverity,
  val title: String,
  val deviceIp: String,
  val sourceAddress: String,
  val destinationAddress: String,
  val protocol: String,
  val port: Int,
  val timestamp: Long,
  val timeFormatted: String,
  val evidence: String,
  val confidence: String, // High, Medium, Low
  val explanation: String
)

data class NetworkGraphNode(
  val id: String,
  val label: String,
  val ipAddress: String,
  val type: DeviceType,
  val isCenter: Boolean = false,
  val totalTrafficBytes: Long = 0L
)

data class NetworkGraphEdge(
  val sourceId: String,
  val targetId: String,
  val protocol: String,
  val bytes: Long,
  val packetCount: Long,
  val isEncrypted: Boolean = true
)

data class RealTimeTrafficStats(
  val packetsPerSec: Double = 0.0,
  val bytesPerSec: Double = 0.0,
  val uploadBytesPerSec: Double = 0.0,
  val downloadBytesPerSec: Double = 0.0,
  val totalPackets: Long = 0L,
  val totalBytes: Long = 0L,
  val totalUploadBytes: Long = 0L,
  val totalDownloadBytes: Long = 0L,
  val tcpPackets: Long = 0L,
  val udpPackets: Long = 0L,
  val icmpPackets: Long = 0L,
  val dnsPackets: Long = 0L,
  val tlsPackets: Long = 0L,
  val quicPackets: Long = 0L,
  val otherProtocolsPackets: Long = 0L
)

data class AiAnalystInsight(
  val generatedAtFormatted: String = "",
  val networkSummary: String = "",
  val observableDevicesInsight: String = "",
  val topServicesInsight: String = "",
  val securityFindings: String = "",
  val healthAssessment: String = "",
  val recommendations: List<String> = emptyList(),
  val isGenerating: Boolean = false,
  val rawResponse: String = ""
)
