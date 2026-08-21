package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 37. NETWORK SESSION ENTITY
 * Persists every live network capture / monitoring session.
 */
@Entity(
  tableName = "network_sessions",
  indices = [
    Index(value = ["sessionId"], unique = true),
    Index(value = ["startTime"]),
    Index(value = ["networkName"]),
    Index(value = ["interfaceName"])
  ]
)
data class NetworkSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val startTime: Long = System.currentTimeMillis(),
  val endTime: Long? = null,
  val networkName: String, // SSID or Network label
  val interfaceName: String, // wlan0, tun0, rmnet0
  val interfaceType: String, // Wi-Fi, VPN, Cellular, Ethernet
  val localIp: String,
  val ipv6: String,
  val macAddress: String,
  val gateway: String,
  val dnsServers: String, // Comma-separated list
  val subnet: String,
  val captureStatus: String = "ACTIVE", // ACTIVE, COMPLETED, STOPPED
  val totalPackets: Long = 0L,
  val totalBytes: Long = 0L,
  val uploadBytes: Long = 0L,
  val downloadBytes: Long = 0L
)

/**
 * 39. DEVICE DATABASE ENTITY
 * Persists legitimately observed devices across networks and sessions.
 */
@Entity(
  tableName = "network_devices",
  indices = [
    Index(value = ["deviceId"], unique = true),
    Index(value = ["ipAddress"]),
    Index(value = ["macAddress"]),
    Index(value = ["hostname"]),
    Index(value = ["lastSeen"])
  ]
)
data class NetworkDeviceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val deviceId: String, // IP or MAC based unique ID
  val ipAddress: String,
  val ipv6: String = "",
  val macAddress: String = "Not observable",
  val hostname: String = "Not observable",
  val vendor: String = "Unknown Vendor",
  val deviceType: String = "UNKNOWN",
  val firstSeen: Long = System.currentTimeMillis(),
  val lastSeen: Long = System.currentTimeMillis(),
  val isActive: Boolean = true
)

/**
 * 40. DEVICE SESSION HISTORY ENTITY
 * Correlates observed devices per specific session with metrics.
 */
@Entity(
  tableName = "device_session_history",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["deviceId"]),
    Index(value = ["sessionId", "deviceId"])
  ]
)
data class DeviceSessionHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val deviceId: String,
  val ipAddress: String,
  val firstSeen: Long = System.currentTimeMillis(),
  val lastSeen: Long = System.currentTimeMillis(),
  val packets: Long = 0L,
  val bytes: Long = 0L,
  val upload: Long = 0L,
  val download: Long = 0L,
  val activeConnections: Int = 0,
  val protocols: String = "", // Comma-separated list
  val ports: String = "" // Comma-separated list
)

/**
 * 42. TRAFFIC STATISTICS TABLE
 * Time-bucketed aggregate traffic metrics (every 1s, 5s, or 1m) for historical graphs.
 */
@Entity(
  tableName = "traffic_statistics",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["timestamp"]),
    Index(value = ["protocol"])
  ]
)
data class TrafficStatisticEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val device: String = "All Devices",
  val protocol: String = "ALL",
  val bytes: Long = 0L,
  val packets: Long = 0L,
  val upload: Long = 0L,
  val download: Long = 0L,
  val connections: Int = 0
)

/**
 * 43. APPLICATION / SERVICE OBSERVATIONS ENTITY
 * Real observed or inferred application services with strict evidence.
 */
@Entity(
  tableName = "service_observations",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["deviceId"]),
    Index(value = ["serviceName"]),
    Index(value = ["classification"]),
    Index(value = ["timestamp"])
  ]
)
data class ServiceObservationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val deviceId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val serviceName: String,
  val domain: String = "",
  val destinationIp: String = "",
  val protocol: String = "TLS",
  val port: Int = 443,
  val trafficBytes: Long = 0L,
  val classification: String = "OBSERVED", // OBSERVED, INFERRED, UNKNOWN
  val confidence: String = "High",
  val evidence: String = ""
)

/**
 * 44. DNS HISTORY ENTITY
 * Persists legitimate observed DNS lookups.
 */
@Entity(
  tableName = "dns_history",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["deviceId"]),
    Index(value = ["domain"]),
    Index(value = ["timestamp"])
  ]
)
data class DnsHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val deviceId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val dnsServer: String,
  val domain: String,
  val queryType: String = "A",
  val response: String = "",
  val responseStatus: String = "NOERROR",
  val responseTimeMs: Long = 0L
)

/**
 * 45. CONNECTION HISTORY ENTITY
 * Flow tracking with indexes for high-speed multi-attribute filtering.
 */
@Entity(
  tableName = "connection_history",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["deviceId"]),
    Index(value = ["timestamp"]),
    Index(value = ["sourceIp"]),
    Index(value = ["destinationIp"]),
    Index(value = ["protocol"]),
    Index(value = ["destinationPort"])
  ]
)
data class ConnectionHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val deviceId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val sourceIp: String,
  val destinationIp: String,
  val sourcePort: Int,
  val destinationPort: Int,
  val protocol: String,
  val bytes: Long,
  val packets: Long,
  val duration: Double,
  val status: String = "ACTIVE"
)

/**
 * 46. SECURITY EVENTS ENTITY
 * Verified security analysis findings backed by real evidence.
 */
@Entity(
  tableName = "security_events",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["deviceId"]),
    Index(value = ["severity"]),
    Index(value = ["eventType"]),
    Index(value = ["timestamp"])
  ]
)
data class SecurityEventEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val eventId: String,
  val sessionId: String,
  val deviceId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val severity: String, // INFORMATIONAL, LOW, MEDIUM, HIGH
  val eventType: String, // PORT_SCAN_PATTERN, UNUSUAL_TRAFFIC, DNS_ANOMALY, etc.
  val source: String,
  val destination: String,
  val protocol: String,
  val port: Int,
  val evidence: String,
  val confidence: String,
  val description: String,
  val status: String = "NEW"
)

/**
 * 47. NETWORK HEALTH HISTORY ENTITY
 * Historical health measurements across sessions.
 */
@Entity(
  tableName = "network_health_history",
  indices = [
    Index(value = ["sessionId"]),
    Index(value = ["timestamp"])
  ]
)
data class NetworkHealthHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val latency: Double,
  val packetLoss: Double,
  val dnsLatency: Double,
  val throughput: Double,
  val retransmissions: Long,
  val connectionFailures: Int,
  val interfaceErrors: Long,
  val healthScore: Int
)

/**
 * 54. DATA RETENTION CONFIGURATION ENTITY
 */
@Entity(tableName = "data_retention_settings")
data class DataRetentionSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val rawPacketsRetentionHours: Int = 24, // 1, 6, 24, 168 (7d), 720 (30d)
  val trafficStatsRetentionDays: Int = 30, // 7, 30, 90, 365
  val securityEventsRetentionDays: Int = 90 // 30, 90, 365
)

// Schema aliases matching domain naming requirements
typealias PacketSession = NetworkSessionEntity
typealias DeviceEntry = NetworkDeviceEntity
typealias TrafficStat = TrafficStatisticEntity
typealias SecurityEvent = SecurityEventEntity

