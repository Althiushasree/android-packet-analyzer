package com.example.data.db

import android.content.Context
import android.util.Log
import com.example.data.intelligence.ApplicationServiceAnalysis
import com.example.data.intelligence.CommunicationFlow
import com.example.data.intelligence.DefensiveSecurityAlert
import com.example.data.intelligence.NetworkHealthReport
import com.example.data.intelligence.ObservedNetworkDevice
import com.example.data.intelligence.RealDnsLogEntry
import com.example.data.intelligence.RealNetworkInterfaceInfo
import com.example.data.intelligence.RealTimeTrafficStats
import com.example.data.model.ConnectionHistoryEntity
import com.example.data.model.DataRetentionSettingsEntity
import com.example.data.model.DeviceSessionHistoryEntity
import com.example.data.model.DnsHistoryEntity
import com.example.data.model.NetworkDeviceEntity
import com.example.data.model.NetworkHealthHistoryEntity
import com.example.data.model.NetworkSessionEntity
import com.example.data.model.SecurityEventEntity
import com.example.data.model.ServiceObservationEntity
import com.example.data.model.TrafficStatisticEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * 51. REAL-TIME DATABASE PIPELINE & PERSISTENCE MANAGER
 * Bridges live packet capture, network observation pipeline, and Room SQLite database.
 * Supports:
 * - Real-time batching and non-blocking background writes
 * - Historical session querying and correlation
 * - Data retention automatic enforcement and cleanup
 * - Database health / status telemetry (Connected / Disconnected / Storage size)
 */
class NetworkDatabaseManager(private val context: Context) {
  private val db = AppDatabase.getDatabase(context)
  private val scope = CoroutineScope(Dispatchers.IO)

  // DAOs
  val sessionDao = db.networkSessionDao()
  val deviceDao = db.networkDeviceDao()
  val deviceHistoryDao = db.deviceSessionHistoryDao()
  val trafficStatDao = db.trafficStatisticDao()
  val serviceObservationDao = db.serviceObservationDao()
  val dnsHistoryDao = db.dnsHistoryDao()
  val connectionHistoryDao = db.connectionHistoryDao()
  val securityEventDao = db.securityEventDao()
  val healthHistoryDao = db.networkHealthHistoryDao()
  val retentionDao = db.dataRetentionDao()
  val packetDao = db.packetDao()

  // Status & Telemetry (55. DATABASE STORAGE STATUS)
  private val _isDbConnected = MutableStateFlow(true)
  val isDbConnected: StateFlow<Boolean> = _isDbConnected.asStateFlow()

  private val _lastWriteTimestamp = MutableStateFlow(System.currentTimeMillis())
  val lastWriteTimestamp: StateFlow<Long> = _lastWriteTimestamp.asStateFlow()

  private val _totalRecordsCount = MutableStateFlow(0)
  val totalRecordsCount: StateFlow<Int> = _totalRecordsCount.asStateFlow()

  private val _databaseSizeBytes = MutableStateFlow(0L)
  val databaseSizeBytes: StateFlow<Long> = _databaseSizeBytes.asStateFlow()

  private val _activeSessionId = MutableStateFlow<String?>(null)
  val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

  val allSessions: Flow<List<NetworkSessionEntity>> = sessionDao.getAllSessionsFlow()
  val allDevices: Flow<List<NetworkDeviceEntity>> = deviceDao.getAllDevicesFlow()
  val retentionSettings: Flow<DataRetentionSettingsEntity> = retentionDao.getRetentionSettingsFlow().map {
    it ?: DataRetentionSettingsEntity()
  }

  init {
    scope.launch {
      updateDatabaseMetrics()
    }
  }

  /**
   * 37. Starts or records a new Network Session upon network change or capture start.
   */
  fun startOrRegisterSession(netInfo: RealNetworkInterfaceInfo): String {
    val sessionId = "SESSION-" + UUID.randomUUID().toString().take(8).uppercase()
    _activeSessionId.value = sessionId

    scope.launch {
      try {
        val sessionEntity = NetworkSessionEntity(
          sessionId = sessionId,
          startTime = System.currentTimeMillis(),
          networkName = if (netInfo.ssid.isNotBlank() && !netInfo.ssid.contains("Not observable")) netInfo.ssid else "${netInfo.interfaceType} (${netInfo.interfaceName})",
          interfaceName = netInfo.interfaceName,
          interfaceType = netInfo.interfaceType,
          localIp = netInfo.localIpv4,
          ipv6 = netInfo.localIpv6,
          macAddress = netInfo.macAddress,
          gateway = netInfo.defaultGateway,
          dnsServers = netInfo.dnsServers.joinToString(", "),
          subnet = netInfo.subnetMask,
          captureStatus = "ACTIVE",
          totalPackets = netInfo.rxPackets + netInfo.txPackets,
          totalBytes = netInfo.rxBytes + netInfo.txBytes,
          uploadBytes = netInfo.txBytes,
          downloadBytes = netInfo.rxBytes
        )
        sessionDao.insertSession(sessionEntity)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
        updateDatabaseMetrics()
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error saving session start", e)
        _isDbConnected.value = false
      }
    }
    return sessionId
  }

  /**
   * Closes the active network session with final tally.
   */
  fun closeActiveSession(totalPackets: Long, totalBytes: Long, uploadBytes: Long, downloadBytes: Long) {
    val sid = _activeSessionId.value ?: return
    scope.launch {
      try {
        sessionDao.closeSession(
          sessionId = sid,
          endTime = System.currentTimeMillis(),
          totalPackets = totalPackets,
          totalBytes = totalBytes,
          uploadBytes = uploadBytes,
          downloadBytes = downloadBytes
        )
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error closing session", e)
      }
    }
  }

  /**
   * 39 & 40. Persists real observed devices and their session usage.
   */
  fun recordObservedDevices(sessionId: String, devices: List<ObservedNetworkDevice>) {
    if (devices.isEmpty()) return
    scope.launch {
      try {
        val now = System.currentTimeMillis()
        val deviceEntities = devices.map { d ->
          NetworkDeviceEntity(
            deviceId = d.id,
            ipAddress = d.ipAddress,
            macAddress = d.macAddress,
            hostname = d.hostname,
            vendor = d.vendor,
            deviceType = d.estimatedDeviceType.name,
            firstSeen = d.firstSeenTimestamp,
            lastSeen = now,
            isActive = d.isActive
          )
        }
        deviceDao.insertDevices(deviceEntities)

        val historyEntities = devices.map { d ->
          DeviceSessionHistoryEntity(
            sessionId = sessionId,
            deviceId = d.id,
            ipAddress = d.ipAddress,
            firstSeen = d.firstSeenTimestamp,
            lastSeen = now,
            packets = d.totalPackets,
            bytes = d.totalBytes,
            upload = d.uploadBytes,
            download = d.downloadBytes,
            activeConnections = d.activeConnectionsCount,
            protocols = d.observedProtocols.joinToString(", "),
            ports = d.openPorts.joinToString(", ")
          )
        }
        deviceHistoryDao.insertBatch(historyEntities)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error persisting devices", e)
      }
    }
  }

  /**
   * 42. Persists aggregated time-series traffic stats for historical plotting.
   */
  fun recordTrafficStatistic(sessionId: String, stats: RealTimeTrafficStats) {
    scope.launch {
      try {
        val statEntity = TrafficStatisticEntity(
          sessionId = sessionId,
          timestamp = System.currentTimeMillis(),
          device = "All Devices",
          protocol = "ALL",
          bytes = stats.totalBytes,
          packets = stats.totalPackets,
          upload = stats.totalUploadBytes,
          download = stats.totalDownloadBytes,
          connections = 0
        )
        trafficStatDao.insertStatistic(statEntity)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error writing traffic stat", e)
      }
    }
  }

  /**
   * 43. Persists application / service observations.
   */
  fun recordServiceObservations(sessionId: String, services: List<ApplicationServiceAnalysis>) {
    if (services.isEmpty()) return
    scope.launch {
      try {
        val entities = services.map { s ->
          ServiceObservationEntity(
            sessionId = sessionId,
            deviceId = s.deviceIp,
            timestamp = System.currentTimeMillis(),
            serviceName = s.serviceName,
            domain = s.domainName,
            destinationIp = s.deviceIp,
            protocol = s.protocol,
            port = s.portsUsed.firstOrNull() ?: 443,
            trafficBytes = s.trafficBytes,
            classification = s.status.name, // OBSERVED, INFERRED, UNKNOWN
            confidence = "High",
            evidence = s.evidence
          )
        }
        serviceObservationDao.insertBatch(entities)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error saving service observations", e)
      }
    }
  }

  /**
   * 44. Persists DNS transaction observations.
   */
  fun recordDnsLogs(sessionId: String, logs: List<RealDnsLogEntry>) {
    if (logs.isEmpty()) return
    scope.launch {
      try {
        val entities = logs.map { l ->
          DnsHistoryEntity(
            sessionId = sessionId,
            deviceId = l.deviceIp,
            timestamp = l.timestamp,
            dnsServer = l.dnsServer,
            domain = l.queryDomain,
            queryType = l.queryType,
            response = l.responseAnswer,
            responseStatus = if (l.isSuccess) "NOERROR" else "NXDOMAIN",
            responseTimeMs = l.latencyMs
          )
        }
        dnsHistoryDao.insertBatch(entities)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error persisting DNS logs", e)
      }
    }
  }

  /**
   * 45. Persists active communication flows.
   */
  fun recordCommunicationFlows(sessionId: String, flows: List<CommunicationFlow>) {
    if (flows.isEmpty()) return
    scope.launch {
      try {
        val entities = flows.map { f ->
          ConnectionHistoryEntity(
            sessionId = sessionId,
            deviceId = f.sourceDeviceIp,
            timestamp = f.lastSeenTimestamp,
            sourceIp = f.sourceDeviceIp,
            destinationIp = f.destinationAddress,
            sourcePort = 0,
            destinationPort = f.port,
            protocol = f.protocol,
            bytes = f.totalBytes,
            packets = f.packetCount,
            duration = 0.0,
            status = f.status.name
          )
        }
        connectionHistoryDao.insertBatch(entities)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error persisting flows", e)
      }
    }
  }

  /**
   * 46. Persists Defensive Security Alerts.
   */
  fun recordSecurityAlerts(sessionId: String, alerts: List<DefensiveSecurityAlert>) {
    if (alerts.isEmpty()) return
    scope.launch {
      try {
        val entities = alerts.map { a ->
          SecurityEventEntity(
            eventId = a.id,
            sessionId = sessionId,
            deviceId = a.deviceIp,
            timestamp = a.timestamp,
            severity = a.severity.name,
            eventType = a.title,
            source = a.sourceAddress,
            destination = a.destinationAddress,
            protocol = a.protocol,
            port = a.port,
            evidence = a.evidence,
            confidence = a.confidence,
            description = a.explanation,
            status = "NEW"
          )
        }
        securityEventDao.insertBatch(entities)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error saving security alerts", e)
      }
    }
  }

  /**
   * 47. Persists Network Health snapshot.
   */
  fun recordHealthReport(sessionId: String, health: NetworkHealthReport) {
    scope.launch {
      try {
        val entity = NetworkHealthHistoryEntity(
          sessionId = sessionId,
          timestamp = health.measurementTimestamp,
          latency = health.gatewayLatencyMs,
          packetLoss = health.packetLossPercent,
          dnsLatency = health.dnsLatencyMs,
          throughput = health.throughputMbps,
          retransmissions = health.retransmissionCount,
          connectionFailures = health.connectionFailures,
          interfaceErrors = health.interfaceErrors,
          healthScore = health.healthScore
        )
        healthHistoryDao.insertHealth(entity)
        _lastWriteTimestamp.value = System.currentTimeMillis()
        _isDbConnected.value = true
      } catch (e: Exception) {
        Log.e("NetworkDatabaseManager", "Error saving health history", e)
      }
    }
  }

  /**
   * 54. DATA RETENTION ENFORCEMENT
   */
  suspend fun enforceDataRetention(settings: DataRetentionSettingsEntity) = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()

    // 1. Raw packets retention
    val packetCutoff = now - (settings.rawPacketsRetentionHours * 3600 * 1000L)
    packetDao.deletePacketsOlderThan(packetCutoff)

    // 2. Traffic stats retention
    val statCutoff = now - (settings.trafficStatsRetentionDays * 86400 * 1000L)
    trafficStatDao.deleteOlderThan(statCutoff)

    // 3. Security events retention
    val secCutoff = now - (settings.securityEventsRetentionDays * 86400 * 1000L)
    securityEventDao.deleteOlderThan(secCutoff)

    retentionDao.saveRetentionSettings(settings)
    updateDatabaseMetrics()
  }

  suspend fun updateDatabaseMetrics() = withContext(Dispatchers.IO) {
    try {
      val pCount = packetDao.getPacketCount()
      val sCount = sessionDao.getSessionCount()
      val dCount = deviceDao.getDeviceCount()
      val secCount = securityEventDao.getSecurityEventCount()

      _totalRecordsCount.value = pCount + sCount + dCount + secCount

      val dbFile = context.getDatabasePath("packet_capture_pro.db")
      if (dbFile.exists()) {
        _databaseSizeBytes.value = dbFile.length()
      }
      _isDbConnected.value = true
    } catch (e: Exception) {
      Log.e("NetworkDatabaseManager", "Error updating DB metrics", e)
      _isDbConnected.value = false
    }
  }

  /**
   * Query full details for a historical session
   */
  suspend fun getHistoricalSessionDetails(sessionId: String): HistoricalSessionDetails = withContext(Dispatchers.IO) {
    val session = sessionDao.getSessionById(sessionId)
    val devices = deviceHistoryDao.getDevicesForSession(sessionId)
    val stats = trafficStatDao.getTrafficStatsForSession(sessionId)
    val services = serviceObservationDao.getServicesForSession(sessionId)
    val securityEvents = securityEventDao.getSecurityEventsForSession(sessionId)
    val healthRecords = healthHistoryDao.getHealthHistoryForSession(sessionId)

    HistoricalSessionDetails(
      session = session,
      devices = devices,
      stats = stats,
      services = services,
      securityEvents = securityEvents,
      healthRecords = healthRecords
    )
  }
}

data class HistoricalSessionDetails(
  val session: NetworkSessionEntity?,
  val devices: List<DeviceSessionHistoryEntity> = emptyList(),
  val stats: List<TrafficStatisticEntity> = emptyList(),
  val services: List<ServiceObservationEntity> = emptyList(),
  val securityEvents: List<SecurityEventEntity> = emptyList(),
  val healthRecords: List<NetworkHealthHistoryEntity> = emptyList()
)
