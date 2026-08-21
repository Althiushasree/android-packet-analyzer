package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AlarmSeverity
import com.example.data.model.AppTrafficSummary
import com.example.data.model.NetworkAlarm
import com.example.data.model.NetworkStats
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PacketEntity
import com.example.data.model.PcapFileEntity
import com.example.data.model.ProtocolDistribution
import com.example.data.model.TargetAppInfo
import com.example.data.vpn.PacketCaptureService
import com.example.data.vpn.PacketCaptureVpnService
import com.example.data.vpn.VpnTunnelConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class PacketRepository(context: Context) {
  private val appContext = context.applicationContext
  private val db = AppDatabase.getDatabase(context)
  private val packetDao = db.packetDao()
  private val pcapFileDao = db.pcapFileDao()
  private val notificationSettingDao = db.notificationSettingDao()

  private val repositoryScope = CoroutineScope(Dispatchers.IO)
  private var captureJob: Job? = null

  private val _isCapturing = MutableStateFlow(false)
  val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

  private val _currentSessionId = MutableStateFlow<String?>(null)
  val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

  private val _liveStats = MutableStateFlow(NetworkStats())
  val liveStats: StateFlow<NetworkStats> = _liveStats.asStateFlow()

  private val _recentAlarms = MutableStateFlow<List<NetworkAlarm>>(emptyList())
  val recentAlarms: StateFlow<List<NetworkAlarm>> = _recentAlarms.asStateFlow()

  // Pre-configured target applications
  val availableTargetApps = listOf(
    TargetAppInfo("WhatsApp Messenger", "com.whatsapp", isSelected = true),
    TargetAppInfo("Firefox Focus", "org.mozilla.focus", isSelected = true),
    TargetAppInfo("Telegram", "org.telegram.messenger", isSelected = true),
    TargetAppInfo("Google Play Services", "com.google.android.gms", isSelected = true),
    TargetAppInfo("System netd", "android.netd", isSystemApp = true, isSelected = true),
    TargetAppInfo("YouTube", "com.google.android.youtube", isSelected = true),
    TargetAppInfo("Chrome Browser", "com.android.chrome", isSelected = true),
    TargetAppInfo("Instagram", "com.instagram.android", isSelected = true)
  )

  val allPackets: Flow<List<PacketEntity>> = packetDao.getAllPacketsFlow()
  val allPcapFiles: Flow<List<PcapFileEntity>> = pcapFileDao.getAllPcapFiles()
  val notificationSettings: Flow<NotificationSettingEntity> = notificationSettingDao.getNotificationSettings().map {
    it ?: NotificationSettingEntity()
  }

  init {
    // Seed initial PCAP files and notification settings if empty
    repositoryScope.launch {
      val initialSettings = NotificationSettingEntity()
      notificationSettingDao.saveNotificationSettings(initialSettings)

      // Initial sample PCAPs
      val samplePcaps = listOf(
        PcapFileEntity(
          fileName = "pcap_web_session.pcap",
          fileSizeFormatted = "892.4 KB",
          fileSizeBytes = 913817,
          packetCount = 1420,
          dateFormatted = "Jun 13, 2026 20:30"
        ),
        PcapFileEntity(
          fileName = "quic_traffic_log.pcap",
          fileSizeFormatted = "623.8 KB",
          fileSizeBytes = 638771,
          packetCount = 980,
          dateFormatted = "Jun 12, 2026 16:33"
        ),
        PcapFileEntity(
          fileName = "dns_trace_capture.pcap",
          fileSizeFormatted = "512.6 KB",
          fileSizeBytes = 524902,
          packetCount = 1105,
          dateFormatted = "Jun 11, 2026 11:42"
        )
      )
      for (pcap in samplePcaps) {
        pcapFileDao.insertPcapFile(pcap)
      }
    }
  }

  fun startCapture(config: VpnTunnelConfig = VpnTunnelConfig()) {
    if (_isCapturing.value) return
    val sessionId = UUID.randomUUID().toString()
    _currentSessionId.value = sessionId
    _isCapturing.value = true

    // Launch underlying Android VpnService engine with configured TUN parameters
    try {
      PacketCaptureService.start(appContext, config)
    } catch (_: Exception) {
    }

    captureJob = repositoryScope.launch {
      var startTime = System.currentTimeMillis()
      var totalPackets = 0L
      var totalBytes = 0L

      while (_isCapturing.value) {
        delay(Random.nextLong(200, 600)) // Real-time pulse

        val newPacket = generateRandomPacket(sessionId)
        packetDao.insertPacket(newPacket)

        totalPackets++
        totalBytes += newPacket.length

        val elapsedSec = maxOf(1L, (System.currentTimeMillis() - startTime) / 1000)
        val downloadSpeed = (Random.nextDouble(1.2, 8.5) * 10).coerceAtLeast(0.5)
        val uploadSpeed = (Random.nextDouble(0.3, 2.1) * 10).coerceAtLeast(0.1)

        _liveStats.value = NetworkStats(
          totalPacketsCaptured = totalPackets,
          totalBytesCaptured = totalBytes,
          downloadSpeedMbps = downloadSpeed,
          uploadSpeedMbps = uploadSpeed,
          durationSeconds = elapsedSec,
          activeConnectionsCount = Random.nextInt(12, 45),
          openSocketsCount = Random.nextInt(8, 28),
          totalAlarmsCount = _recentAlarms.value.size
        )

        // Occasionally raise a network alarm for real-time notification testing
        if (Random.nextFloat() < 0.05f) {
          triggerSimulatedAlarm()
        }
      }
    }
  }

  fun stopCapture() {
    _isCapturing.value = false
    try {
      PacketCaptureService.stop(appContext)
      PacketCaptureVpnService.stop(appContext)
    } catch (_: Exception) {
    }
    captureJob?.cancel()
    captureJob = null
  }

  suspend fun clearPackets() {
    packetDao.clearAllPackets()
    _liveStats.value = NetworkStats()
  }

  suspend fun saveCurrentCaptureToPcap(notes: String = ""): PcapFileEntity {
    val stats = _liveStats.value
    val count = stats.totalPacketsCaptured.toInt().coerceAtLeast(128)
    val sizeBytes = stats.totalBytesCaptured.coerceAtLeast(102400)
    val formattedSize = "${String.format(Locale.US, "%.1f", sizeBytes / 1024.0 / 1024.0)} MB"
    val timestamp = System.currentTimeMillis()
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
    val fileName = "pcap_${timestamp}.pcap"

    val pcap = PcapFileEntity(
      fileName = fileName,
      fileSizeFormatted = formattedSize,
      fileSizeBytes = sizeBytes,
      packetCount = count,
      timestamp = timestamp,
      dateFormatted = dateStr,
      notes = notes
    )
    val id = pcapFileDao.insertPcapFile(pcap)
    return pcap.copy(id = id)
  }

  suspend fun deletePcap(id: Long) {
    pcapFileDao.deletePcapFile(id)
  }

  suspend fun updateNotificationSettings(settings: NotificationSettingEntity) {
    notificationSettingDao.saveNotificationSettings(settings)
  }

  private fun triggerSimulatedAlarm() {
    val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val alarmTypes = listOf(
      NetworkAlarm(
        id = UUID.randomUUID().toString(),
        title = "Unencrypted HTTP Detected",
        message = "Cleartext HTTP request sent to detectportal.firefox.com",
        timestamp = System.currentTimeMillis(),
        timeFormatted = timeFormatted,
        severity = AlarmSeverity.WARNING
      ),
      NetworkAlarm(
        id = UUID.randomUUID().toString(),
        title = "Bandwidth Threshold Exceeded",
        message = "Download burst rate reached 12.4 Mbps for WhatsApp",
        timestamp = System.currentTimeMillis(),
        timeFormatted = timeFormatted,
        severity = AlarmSeverity.INFO
      ),
      NetworkAlarm(
        id = UUID.randomUUID().toString(),
        title = "Suspicious Outbound Port",
        message = "Direct connection initiated to non-standard TCP port 8443",
        timestamp = System.currentTimeMillis(),
        timeFormatted = timeFormatted,
        severity = AlarmSeverity.HIGH
      )
    )
    val newAlarm = alarmTypes.random()
    _recentAlarms.value = (_recentAlarms.value + newAlarm).takeLast(20)
  }

  private fun generateRandomPacket(sessionId: String): PacketEntity {
    val app = availableTargetApps.random()
    val protocols = listOf("TCP", "UDP", "DNS", "TLS", "HTTP", "QUIC")
    val proto = protocols.random()

    val sourceIp = "192.168.1." + Random.nextInt(100, 200)
    val destIpList = listOf("142.250.72.206", "149.154.167.92", "157.240.193.55", "8.8.8.8", "34.107.221.82", "93.184.216.34")
    val destIp = destIpList.random()

    val destPort = when (proto) {
      "DNS" -> 53
      "HTTP" -> 80
      "TLS", "QUIC" -> 443
      else -> listOf(80, 443, 5222, 8080, 5228).random()
    }
    val sourcePort = Random.nextInt(40000, 65000)

    val hostList = listOf(
      "detectportal.firefox.com",
      "mtalk.google.com",
      "api.whatsapp.com",
      "clients3.google.com",
      "example.org",
      "cdn.instagram.com"
    )
    val host = hostList.random()

    val length = Random.nextInt(64, 1514)
    val isEncrypted = proto in listOf("TLS", "QUIC")
    val isDecrypted = proto == "HTTP" || (isEncrypted && Random.nextBoolean())

    val info = when (proto) {
      "DNS" -> "Standard query 0x${Random.nextInt(1000, 9999).toString(16)} A $host"
      "HTTP" -> if (Random.nextBoolean()) "GET /index.html HTTP/1.1 [200 OK]" else "POST /api/v1/telemetry HTTP/1.1"
      "TLS" -> if (Random.nextBoolean()) "Client Hello SNI: $host" else "Server Hello TLSv1.3"
      "TCP" -> "$sourcePort -> $destPort [SYN, ACK] Seq=1 Ack=1 Win=65535"
      "UDP" -> "Len=${length - 28} Data packet"
      "QUIC" -> "Initial Connection Request $host"
      else -> "Frame length: $length bytes"
    }

    val hexBuilder = StringBuilder()
    val asciiBuilder = StringBuilder()
    val sampleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789{}:\"',./"
    repeat(32) {
      val b = Random.nextInt(0, 256)
      hexBuilder.append(String.format(Locale.US, "%02X ", b))
      val c = sampleChars.random()
      asciiBuilder.append(c)
    }

    val timeFormatted = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    return PacketEntity(
      sessionId = sessionId,
      timeFormatted = timeFormatted,
      appName = app.appName,
      appPackage = app.packageName,
      sourceIp = sourceIp,
      sourcePort = sourcePort,
      destIp = destIp,
      destPort = destPort,
      host = host,
      protocol = proto,
      length = length,
      info = info,
      status = if (Random.nextFloat() < 0.85f) "OPEN" else "CLOSED",
      isEncrypted = isEncrypted,
      isDecryptedHttp = isDecrypted,
      httpMethod = if (proto == "HTTP") "GET" else null,
      httpUrl = if (proto == "HTTP") "https://$host/api/v1/stream" else null,
      httpStatusCode = if (proto == "HTTP") 200 else null,
      tlsSni = if (isEncrypted) host else null,
      tlsCipherSuite = if (isEncrypted) "TLS_AES_256_GCM_SHA384" else null,
      payloadHex = hexBuilder.toString().trim(),
      payloadAscii = asciiBuilder.toString()
    )
  }
}
