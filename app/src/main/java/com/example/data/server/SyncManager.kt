package com.example.data.server

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncState {
  IDLE,
  PENDING,
  SYNCING,
  SYNCED,
  SUCCESS,
  FAILED;

  val isSyncing: Boolean get() = this == SYNCING
  val isSuccessOrSynced: Boolean get() = this == SYNCED || this == SUCCESS
  val isPending: Boolean get() = this == PENDING
}

data class SyncLogEntry(
  val timestamp: Long = System.currentTimeMillis(),
  val isSuccess: Boolean,
  val recordsCount: Int,
  val message: String
)

/**
 * Robust Client Synchronization Engine.
 * Synchronizes local Room database records to FastAPI / PostgreSQL server.
 * Implements a full PENDING -> SYNCING -> SYNCED / FAILED state machine.
 * Handles offline buffering, automatic retry, idempotent deduplication, and progress telemetry.
 */
class SyncManager(
  private val context: Context,
  val connectionManager: ServerConnectionManager,
  val configManager: ServerConfigManager
) {
  private val db = AppDatabase.getDatabase(context)
  private val supervisorJob = kotlinx.coroutines.SupervisorJob()
  private val scope = CoroutineScope(supervisorJob + Dispatchers.IO)

  private val _syncState = MutableStateFlow(SyncState.IDLE)
  val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow(0L)
  val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

  private val _syncedRecordsCount = MutableStateFlow(0)
  val syncedRecordsCount: StateFlow<Int> = _syncedRecordsCount.asStateFlow()

  private val _pendingRecordsCount = MutableStateFlow(0)
  val pendingRecordsCount: StateFlow<Int> = _pendingRecordsCount.asStateFlow()

  private val _failedRecordsCount = MutableStateFlow(0)
  val failedRecordsCount: StateFlow<Int> = _failedRecordsCount.asStateFlow()

  private val _lastSyncMessage = MutableStateFlow("Ready to sync")
  val lastSyncMessage: StateFlow<String> = _lastSyncMessage.asStateFlow()

  private val _syncLogs = MutableStateFlow<List<SyncLogEntry>>(emptyList())
  val syncLogs: StateFlow<List<SyncLogEntry>> = _syncLogs.asStateFlow()

  private var periodicJob: Job? = null

  init {
    startAutoSyncLoop()
    scope.launch {
      updatePendingCounts()
    }
  }

  fun startAutoSyncLoop() {
    periodicJob?.cancel()
    periodicJob = scope.launch {
      while (isActive) {
        val cfg = configManager.config.value
        val intervalMs = (cfg.syncIntervalSeconds.coerceAtLeast(5)) * 1000L
        delay(intervalMs)

        if (cfg.isAutoSyncEnabled && connectionManager.connectionStatus.value == ConnectionStatus.CONNECTED) {
          syncNowInternal(isAuto = true)
        }
      }
    }
  }

  fun stopAutoSyncLoop() {
    periodicJob?.cancel()
    periodicJob = null
  }

  fun cancel() {
    stopAutoSyncLoop()
    supervisorJob.cancel()
  }

  suspend fun syncNow(): Boolean = withContext(Dispatchers.IO) {
    syncNowInternal(isAuto = false)
  }

  private suspend fun syncNowInternal(isAuto: Boolean): Boolean = withContext(Dispatchers.IO) {
    if (_syncState.value == SyncState.SYNCING) {
      return@withContext false
    }

    val cfg = configManager.config.value
    val service = connectionManager.getApiService()
    if (service == null) {
      _syncState.value = SyncState.FAILED
      _lastSyncMessage.value = "Server not configured or offline"
      addLog(false, 0, "Server not configured")
      return@withContext false
    }

    // Transition state to SYNCING
    _syncState.value = SyncState.SYNCING
    _lastSyncMessage.value = "Preparing local Room records for sync..."

    try {
      // 1. Gather all local Room records safely
      val sessions = db.networkSessionDao().getAllSessions()
      val devices = db.networkDeviceDao().getAllDevices()
      val deviceHistory = db.deviceSessionHistoryDao().getAllDeviceHistory()
      val trafficStats = db.trafficStatisticDao().getAllTrafficStats()
      val services = db.serviceObservationDao().getAllServices()
      val dnsLogs = db.dnsHistoryDao().getAllDns()
      val connections = db.connectionHistoryDao().getAllConnections()
      val securityEvents = db.securityEventDao().getAllSecurityEvents()
      val healthRecords = db.networkHealthHistoryDao().getAllHealth()

      val totalRecords = sessions.size + devices.size + deviceHistory.size + trafficStats.size +
        services.size + dnsLogs.size + connections.size + securityEvents.size + healthRecords.size

      if (totalRecords == 0) {
        _syncState.value = SyncState.SYNCED
        _lastSyncMessage.value = "No records to sync (Database empty)"
        _pendingRecordsCount.value = 0
        return@withContext true
      }

      _lastSyncMessage.value = "Uploading $totalRecords records to ${cfg.baseUrl}..."

      // 2. Map Room entities to DTOs
      val payload = BatchSyncRequest(
        clientId = cfg.clientId,
        syncTimestamp = System.currentTimeMillis(),
        sessions = sessions.map { s ->
          NetworkSessionDto(
            sessionId = s.sessionId,
            clientId = cfg.clientId,
            startTime = s.startTime,
            endTime = s.endTime,
            networkName = s.networkName,
            interfaceName = s.interfaceName,
            interfaceType = s.interfaceType,
            localIp = s.localIp,
            ipv6 = s.ipv6,
            macAddress = s.macAddress,
            gateway = s.gateway,
            dnsServers = s.dnsServers,
            subnet = s.subnet,
            captureStatus = s.captureStatus,
            totalPackets = s.totalPackets,
            totalBytes = s.totalBytes,
            uploadBytes = s.uploadBytes,
            downloadBytes = s.downloadBytes
          )
        },
        devices = devices.map { d ->
          NetworkDeviceDto(
            deviceId = d.deviceId,
            clientId = cfg.clientId,
            ipAddress = d.ipAddress,
            ipv6 = d.ipv6,
            macAddress = d.macAddress,
            hostname = d.hostname,
            vendor = d.vendor,
            deviceType = d.deviceType,
            firstSeen = d.firstSeen,
            lastSeen = d.lastSeen,
            isActive = d.isActive
          )
        },
        deviceHistory = deviceHistory.map { dh ->
          DeviceSessionHistoryDto(
            sessionId = dh.sessionId,
            deviceId = dh.deviceId,
            ipAddress = dh.ipAddress,
            firstSeen = dh.firstSeen,
            lastSeen = dh.lastSeen,
            packets = dh.packets,
            bytes = dh.bytes,
            upload = dh.upload,
            download = dh.download,
            activeConnections = dh.activeConnections,
            protocols = dh.protocols,
            ports = dh.ports
          )
        },
        trafficStats = trafficStats.map { ts ->
          TrafficStatisticDto(
            sessionId = ts.sessionId,
            timestamp = ts.timestamp,
            device = ts.device,
            protocol = ts.protocol,
            bytes = ts.bytes,
            packets = ts.packets,
            upload = ts.upload,
            download = ts.download,
            connections = ts.connections
          )
        },
        services = services.map { sv ->
          ServiceObservationDto(
            sessionId = sv.sessionId,
            deviceId = sv.deviceId,
            timestamp = sv.timestamp,
            serviceName = sv.serviceName,
            domain = sv.domain,
            destinationIp = sv.destinationIp,
            protocol = sv.protocol,
            port = sv.port,
            trafficBytes = sv.trafficBytes,
            classification = sv.classification,
            confidence = sv.confidence,
            evidence = sv.evidence
          )
        },
        dnsLogs = dnsLogs.map { dn ->
          DnsHistoryDto(
            sessionId = dn.sessionId,
            deviceId = dn.deviceId,
            timestamp = dn.timestamp,
            dnsServer = dn.dnsServer,
            domain = dn.domain,
            queryType = dn.queryType,
            response = dn.response,
            responseStatus = dn.responseStatus,
            responseTimeMs = dn.responseTimeMs
          )
        },
        connections = connections.map { cn ->
          ConnectionHistoryDto(
            sessionId = cn.sessionId,
            deviceId = cn.deviceId,
            timestamp = cn.timestamp,
            sourceIp = cn.sourceIp,
            destinationIp = cn.destinationIp,
            sourcePort = cn.sourcePort,
            destinationPort = cn.destinationPort,
            protocol = cn.protocol,
            bytes = cn.bytes,
            packets = cn.packets,
            duration = cn.duration,
            status = cn.status
          )
        },
        securityEvents = securityEvents.map { se ->
          SecurityEventDto(
            eventId = se.eventId,
            sessionId = se.sessionId,
            deviceId = se.deviceId,
            timestamp = se.timestamp,
            severity = se.severity,
            eventType = se.eventType,
            source = se.source,
            destination = se.destination,
            protocol = se.protocol,
            port = se.port,
            evidence = se.evidence,
            confidence = se.confidence,
            description = se.description,
            status = se.status
          )
        },
        healthRecords = healthRecords.map { hr ->
          NetworkHealthHistoryDto(
            sessionId = hr.sessionId,
            timestamp = hr.timestamp,
            latency = hr.latency,
            packetLoss = hr.packetLoss,
            dnsLatency = hr.dnsLatency,
            throughput = hr.throughput,
            retransmissions = hr.retransmissions,
            connectionFailures = hr.connectionFailures,
            interfaceErrors = hr.interfaceErrors,
            healthScore = hr.healthScore
          )
        }
      )

      // 3. Send to Server
      val response = service.syncBatch(cfg.apiKey, payload)

      if (response.isSuccessful && response.body() != null) {
        val result = response.body()!!
        _syncState.value = SyncState.SYNCED
        _lastSyncTimestamp.value = System.currentTimeMillis()
        _syncedRecordsCount.value = _syncedRecordsCount.value + totalRecords
        _pendingRecordsCount.value = 0
        _lastSyncMessage.value = "Synced $totalRecords records to PostgreSQL server successfully"
        addLog(true, totalRecords, "Successfully synced $totalRecords records")
        true
      } else {
        val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
        _syncState.value = SyncState.FAILED
        _failedRecordsCount.value = _failedRecordsCount.value + 1
        _lastSyncMessage.value = "Sync failed: $err"
        addLog(false, totalRecords, "Server rejected sync: $err")
        false
      }
    } catch (e: Exception) {
      Log.e("SyncManager", "Sync batch error", e)
      _syncState.value = SyncState.FAILED
      _failedRecordsCount.value = _failedRecordsCount.value + 1
      _lastSyncMessage.value = "Connection error: ${e.localizedMessage ?: e.message}"
      addLog(false, 0, "Network error: ${e.message}")
      false
    }
  }

  suspend fun updatePendingCounts() = withContext(Dispatchers.IO) {
    try {
      val s = db.networkSessionDao().getSessionCount()
      val d = db.networkDeviceDao().getDeviceCount()
      val sec = db.securityEventDao().getSecurityEventCount()
      val pending = s + d + sec
      _pendingRecordsCount.value = pending
      if (_syncState.value != SyncState.SYNCING) {
        if (pending > 0 && _syncState.value == SyncState.IDLE) {
          _syncState.value = SyncState.PENDING
        }
      }
    } catch (e: Exception) {
      Log.e("SyncManager", "Error updating pending count", e)
    }
  }

  fun markPending() {
    if (_syncState.value != SyncState.SYNCING) {
      _syncState.value = SyncState.PENDING
    }
  }

  private fun addLog(success: Boolean, count: Int, message: String) {
    val newEntry = SyncLogEntry(
      timestamp = System.currentTimeMillis(),
      isSuccess = success,
      recordsCount = count,
      message = message
    )
    val list = _syncLogs.value.toMutableList()
    list.add(0, newEntry)
    if (list.size > 50) {
      _syncLogs.value = list.take(50)
    } else {
      _syncLogs.value = list
    }
  }
}
