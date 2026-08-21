package com.example.data.server

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
  @Json(name = "status") val status: String,
  @Json(name = "database") val database: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "version") val version: String = "1.0.0",
  @Json(name = "clients_count") val clientsCount: Int = 0,
  @Json(name = "sessions_count") val sessionsCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ClientRegisterRequest(
  @Json(name = "client_id") val clientId: String,
  @Json(name = "client_name") val clientName: String,
  @Json(name = "ip_address") val ipAddress: String,
  @Json(name = "os_version") val osVersion: String,
  @Json(name = "app_version") val appVersion: String,
  @Json(name = "device_model") val deviceModel: String
)

@JsonClass(generateAdapter = true)
data class ClientRegisterResponse(
  @Json(name = "status") val status: String,
  @Json(name = "client_id") val clientId: String,
  @Json(name = "registered_at") val registeredAt: Long
)

@JsonClass(generateAdapter = true)
data class NetworkSessionDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "client_id") val clientId: String,
  @Json(name = "start_time") val startTime: Long,
  @Json(name = "end_time") val endTime: Long?,
  @Json(name = "network_name") val networkName: String,
  @Json(name = "interface_name") val interfaceName: String,
  @Json(name = "interface_type") val interfaceType: String,
  @Json(name = "local_ip") val localIp: String,
  @Json(name = "ipv6") val ipv6: String,
  @Json(name = "mac_address") val macAddress: String,
  @Json(name = "gateway") val gateway: String,
  @Json(name = "dns_servers") val dnsServers: String,
  @Json(name = "subnet") val subnet: String,
  @Json(name = "capture_status") val captureStatus: String,
  @Json(name = "total_packets") val totalPackets: Long,
  @Json(name = "total_bytes") val totalBytes: Long,
  @Json(name = "upload_bytes") val uploadBytes: Long,
  @Json(name = "download_bytes") val downloadBytes: Long
)

@JsonClass(generateAdapter = true)
data class NetworkDeviceDto(
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "client_id") val clientId: String,
  @Json(name = "ip_address") val ipAddress: String,
  @Json(name = "ipv6") val ipv6: String,
  @Json(name = "mac_address") val macAddress: String,
  @Json(name = "hostname") val hostname: String,
  @Json(name = "vendor") val vendor: String,
  @Json(name = "device_type") val deviceType: String,
  @Json(name = "first_seen") val firstSeen: Long,
  @Json(name = "last_seen") val lastSeen: Long,
  @Json(name = "is_active") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class DeviceSessionHistoryDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "ip_address") val ipAddress: String,
  @Json(name = "first_seen") val firstSeen: Long,
  @Json(name = "last_seen") val lastSeen: Long,
  @Json(name = "packets") val packets: Long,
  @Json(name = "bytes") val bytes: Long,
  @Json(name = "upload") val upload: Long,
  @Json(name = "download") val download: Long,
  @Json(name = "active_connections") val activeConnections: Int,
  @Json(name = "protocols") val protocols: String,
  @Json(name = "ports") val ports: String
)

@JsonClass(generateAdapter = true)
data class TrafficStatisticDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "device") val device: String,
  @Json(name = "protocol") val protocol: String,
  @Json(name = "bytes") val bytes: Long,
  @Json(name = "packets") val packets: Long,
  @Json(name = "upload") val upload: Long,
  @Json(name = "download") val download: Long,
  @Json(name = "connections") val connections: Int
)

@JsonClass(generateAdapter = true)
data class ServiceObservationDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "service_name") val serviceName: String,
  @Json(name = "domain") val domain: String,
  @Json(name = "destination_ip") val destinationIp: String,
  @Json(name = "protocol") val protocol: String,
  @Json(name = "port") val port: Int,
  @Json(name = "traffic_bytes") val trafficBytes: Long,
  @Json(name = "classification") val classification: String,
  @Json(name = "confidence") val confidence: String,
  @Json(name = "evidence") val evidence: String
)

@JsonClass(generateAdapter = true)
data class DnsHistoryDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "dns_server") val dnsServer: String,
  @Json(name = "domain") val domain: String,
  @Json(name = "query_type") val queryType: String,
  @Json(name = "response") val response: String,
  @Json(name = "response_status") val responseStatus: String,
  @Json(name = "response_time_ms") val responseTimeMs: Long
)

@JsonClass(generateAdapter = true)
data class ConnectionHistoryDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "source_ip") val sourceIp: String,
  @Json(name = "destination_ip") val destinationIp: String,
  @Json(name = "source_port") val sourcePort: Int,
  @Json(name = "destination_port") val destinationPort: Int,
  @Json(name = "protocol") val protocol: String,
  @Json(name = "bytes") val bytes: Long,
  @Json(name = "packets") val packets: Long,
  @Json(name = "duration") val duration: Double,
  @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class SecurityEventDto(
  @Json(name = "event_id") val eventId: String,
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "device_id") val deviceId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "severity") val severity: String,
  @Json(name = "event_type") val eventType: String,
  @Json(name = "source") val source: String,
  @Json(name = "destination") val destination: String,
  @Json(name = "protocol") val protocol: String,
  @Json(name = "port") val port: Int,
  @Json(name = "evidence") val evidence: String,
  @Json(name = "confidence") val confidence: String,
  @Json(name = "description") val description: String,
  @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class NetworkHealthHistoryDto(
  @Json(name = "session_id") val sessionId: String,
  @Json(name = "timestamp") val timestamp: Long,
  @Json(name = "latency") val latency: Double,
  @Json(name = "packet_loss") val packetLoss: Double,
  @Json(name = "dns_latency") val dnsLatency: Double,
  @Json(name = "throughput") val throughput: Double,
  @Json(name = "retransmissions") val retransmissions: Long,
  @Json(name = "connection_failures") val connectionFailures: Int,
  @Json(name = "interface_errors") val interfaceErrors: Long,
  @Json(name = "health_score") val healthScore: Int
)

@JsonClass(generateAdapter = true)
data class BatchSyncRequest(
  @Json(name = "client_id") val clientId: String,
  @Json(name = "sync_timestamp") val syncTimestamp: Long,
  @Json(name = "sessions") val sessions: List<NetworkSessionDto> = emptyList(),
  @Json(name = "devices") val devices: List<NetworkDeviceDto> = emptyList(),
  @Json(name = "device_history") val deviceHistory: List<DeviceSessionHistoryDto> = emptyList(),
  @Json(name = "traffic_stats") val trafficStats: List<TrafficStatisticDto> = emptyList(),
  @Json(name = "services") val services: List<ServiceObservationDto> = emptyList(),
  @Json(name = "dns_logs") val dnsLogs: List<DnsHistoryDto> = emptyList(),
  @Json(name = "connections") val connections: List<ConnectionHistoryDto> = emptyList(),
  @Json(name = "security_events") val securityEvents: List<SecurityEventDto> = emptyList(),
  @Json(name = "health_records") val healthRecords: List<NetworkHealthHistoryDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BatchSyncResponse(
  @Json(name = "status") val status: String,
  @Json(name = "received_at") val receivedAt: Long,
  @Json(name = "synced_records") val syncedRecords: Int,
  @Json(name = "message") val message: String = "Sync successful"
)

@JsonClass(generateAdapter = true)
data class DatabaseStatsResponse(
  @Json(name = "total_clients") val totalClients: Int,
  @Json(name = "total_sessions") val totalSessions: Int,
  @Json(name = "total_devices") val totalDevices: Int,
  @Json(name = "total_traffic_records") val totalTrafficRecords: Int,
  @Json(name = "total_dns_records") val totalDnsRecords: Int,
  @Json(name = "total_connections") val totalConnections: Int,
  @Json(name = "total_security_events") val totalSecurityEvents: Int,
  @Json(name = "total_health_records") val totalHealthRecords: Int,
  @Json(name = "db_status") val dbStatus: String
)
