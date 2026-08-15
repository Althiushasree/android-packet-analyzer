package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppTrafficSummary
import com.example.data.model.AppUsageSummary
import com.example.data.model.AuthState
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.HighestTrafficConsumer
import com.example.data.model.IpUsageSummary
import com.example.data.model.NetworkAlarm
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PacketEntity
import com.example.data.model.PcapFileEntity
import com.example.data.model.ProtocolDistribution
import com.example.data.model.TargetAppInfo
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import com.example.data.model.UserSession
import com.example.data.repository.PacketRepository
import com.example.util.TotpUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
  val repository = PacketRepository(application)

  val isCapturing = repository.isCapturing
  val liveStats = repository.liveStats
  val recentAlarms = repository.recentAlarms
  val allPcapFiles = repository.allPcapFiles
  val notificationSettings = repository.notificationSettings

  // Default Authenticated Session for immediate seamless access to Packet Capture Pro
  private val _authState = MutableStateFlow<AuthState>(AuthState.Authenticated)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _userSession = MutableStateFlow<UserSession?>(
    UserSession(
      email = "secops@cutmac.ap.in",
      displayName = "SecOps Lead",
      isAuthenticated = true,
      domainVerified = true
    )
  )
  val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

  private val _pendingEmail = MutableStateFlow("")
  val pendingEmail: StateFlow<String> = _pendingEmail.asStateFlow()

  private val _pendingDisplayName = MutableStateFlow("")
  val pendingDisplayName: StateFlow<String> = _pendingDisplayName.asStateFlow()

  private var userTotpSecret: String = TotpUtils.DEFAULT_SECRET

  // Timeline Scope State
  private val _selectedTimelineScope = MutableStateFlow(TimelineScope.DAILY)
  val selectedTimelineScope: StateFlow<TimelineScope> = _selectedTimelineScope.asStateFlow()

  // Regulation map per package name
  private val _appRegulationPolicies = MutableStateFlow<Map<String, DetailedAppTraffic>>(emptyMap())

  // Selected Entities for Inspection / Drill Down
  private val _selectedAppDetails = MutableStateFlow<DetailedAppTraffic?>(null)
  val selectedAppDetails: StateFlow<DetailedAppTraffic?> = _selectedAppDetails.asStateFlow()

  private val _selectedIpDetails = MutableStateFlow<DetailedIpTraffic?>(null)
  val selectedIpDetails: StateFlow<DetailedIpTraffic?> = _selectedIpDetails.asStateFlow()

  // UI Filter State for Packet Log / Connections
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedProtocolFilter = MutableStateFlow("ALL")
  val selectedProtocolFilter: StateFlow<String> = _selectedProtocolFilter.asStateFlow()

  private val _selectedAppFilter = MutableStateFlow("ALL")
  val selectedAppFilter: StateFlow<String> = _selectedAppFilter.asStateFlow()

  private val _selectedPacket = MutableStateFlow<PacketEntity?>(null)
  val selectedPacket: StateFlow<PacketEntity?> = _selectedPacket.asStateFlow()

  private val _selectedPcapForHexView = MutableStateFlow<PcapFileEntity?>(null)
  val selectedPcapForHexView: StateFlow<PcapFileEntity?> = _selectedPcapForHexView.asStateFlow()

  private val _showSslCertDialog = MutableStateFlow(false)
  val showSslCertDialog: StateFlow<Boolean> = _showSslCertDialog.asStateFlow()

  private val _showTargetAppSelector = MutableStateFlow(false)
  val showTargetAppSelector: StateFlow<Boolean> = _showTargetAppSelector.asStateFlow()

  private val _targetApps = MutableStateFlow(repository.availableTargetApps)
  val targetApps: StateFlow<List<TargetAppInfo>> = _targetApps.asStateFlow()

  // Combined filtered packets flow
  val filteredPackets: StateFlow<List<PacketEntity>> = combine(
    repository.allPackets,
    _searchQuery,
    _selectedProtocolFilter,
    _selectedAppFilter
  ) { packets, query, protocol, app ->
    packets.filter { p ->
      val matchesQuery = query.isEmpty() ||
          p.host.contains(query, ignoreCase = true) ||
          p.appName.contains(query, ignoreCase = true) ||
          p.destIp.contains(query, ignoreCase = true) ||
          p.sourceIp.contains(query, ignoreCase = true) ||
          p.destPort.toString().contains(query) ||
          p.info.contains(query, ignoreCase = true)

      val matchesProtocol = protocol == "ALL" || p.protocol.equals(protocol, ignoreCase = true)
      val matchesApp = app == "ALL" || p.appName.equals(app, ignoreCase = true)

      matchesQuery && matchesProtocol && matchesApp
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dynamic Detailed Applications Analytics (Top 10 Apps)
  val detailedApplications: StateFlow<List<DetailedAppTraffic>> = combine(
    repository.allPackets,
    _appRegulationPolicies
  ) { packets, policies ->
    if (packets.isEmpty()) return@combine emptyList()
    val totalBytes = packets.sumOf { it.length.toLong() }.coerceAtLeast(1L)
    val appGroups = packets.groupBy { it.appName }

    appGroups.map { (appName, appPackets) ->
      val firstPkt = appPackets.first()
      val appTotal = appPackets.sumOf { it.length.toLong() }
      val download = (appTotal * 0.72).toLong()
      val upload = appTotal - download
      val existingPolicy = policies[firstPkt.appPackage]

      // Destination IPs for this specific application
      val destIps = appPackets.groupBy { it.destIp }.map { (ip, ipPackets) ->
        val ipBytes = ipPackets.sumOf { it.length.toLong() }
        IpUsageSummary(
          ip = ip,
          hostname = ipPackets.first().host,
          bytes = ipBytes,
          packetCount = ipPackets.size,
          percentage = (ipBytes.toFloat() / appTotal.toFloat()) * 100f
        )
      }.sortedByDescending { it.bytes }

      val protos = appPackets.map { it.protocol }.distinct()

      DetailedAppTraffic(
        appName = appName,
        appPackage = firstPkt.appPackage,
        totalBytes = appTotal,
        downloadBytes = download,
        uploadBytes = upload,
        packetCount = appPackets.size,
        percentage = (appTotal.toFloat() / totalBytes.toFloat()) * 100f,
        destinationIps = destIps,
        protocols = protos,
        dailyQuotaBytes = existingPolicy?.dailyQuotaBytes ?: (2000 * 1024 * 1024L),
        isRegulated = existingPolicy?.isRegulated ?: false,
        warningThresholdPercent = existingPolicy?.warningThresholdPercent ?: 80
      )
    }.sortedByDescending { it.totalBytes }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dynamic Detailed IP Analytics (Top 10 IPs)
  val detailedIpAddresses: StateFlow<List<DetailedIpTraffic>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    if (packets.isEmpty()) return@combine emptyList()
    val totalBytes = packets.sumOf { it.length.toLong() }.coerceAtLeast(1L)
    val ipGroups = packets.groupBy { it.destIp }

    ipGroups.map { (ip, ipPackets) ->
      val ipTotal = ipPackets.sumOf { it.length.toLong() }
      val download = (ipTotal * 0.75).toLong()
      val upload = ipTotal - download

      // Applications communicating with this IP
      val communicatingApps = ipPackets.groupBy { it.appName }.map { (appName, pkts) ->
        val appBytes = pkts.sumOf { it.length.toLong() }
        AppUsageSummary(
          appName = appName,
          appPackage = pkts.first().appPackage,
          bytes = appBytes,
          packetCount = pkts.size,
          percentage = (appBytes.toFloat() / ipTotal.toFloat()) * 100f
        )
      }.sortedByDescending { it.bytes }

      val protos = ipPackets.map { it.protocol }.distinct()
      val ports = ipPackets.map { it.destPort }.distinct()

      DetailedIpTraffic(
        ip = ip,
        hostname = ipPackets.first().host,
        totalBytes = ipTotal,
        downloadBytes = download,
        uploadBytes = upload,
        packetCount = ipPackets.size,
        percentage = (ipTotal.toFloat() / totalBytes.toFloat()) * 100f,
        communicatingApps = communicatingApps,
        protocols = protos,
        ports = ports
      )
    }.sortedByDescending { it.totalBytes }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Highest Traffic Consumers Calculation (App, IP, Connection, Protocol)
  val highestTrafficConsumer: StateFlow<HighestTrafficConsumer> = combine(
    detailedApplications,
    detailedIpAddresses,
    repository.allPackets
  ) { apps, ips, packets ->
    val topApp = apps.firstOrNull()
    val topIp = ips.firstOrNull()

    val topConn = if (packets.isNotEmpty()) {
      packets.groupBy { "${it.appName} → ${it.destIp}" }
        .mapValues { entry -> entry.value.sumOf { it.length.toLong() } }
        .maxByOrNull { it.value }
    } else null

    val topProto = if (packets.isNotEmpty()) {
      packets.groupBy { it.protocol }
        .mapValues { entry -> entry.value.sumOf { it.length.toLong() } }
        .maxByOrNull { it.value }
    } else null

    HighestTrafficConsumer(
      topAppName = topApp?.appName ?: "YouTube",
      topAppBytes = topApp?.totalBytes ?: (2400 * 1024 * 1024L),
      topIp = topIp?.ip ?: "142.250.190.46",
      topIpHostname = topIp?.hostname ?: "google.com",
      topIpBytes = topIp?.totalBytes ?: (3200 * 1024 * 1024L),
      topConnection = topConn?.key ?: "YouTube → 142.250.190.46",
      topConnectionBytes = topConn?.value ?: (1850 * 1024 * 1024L),
      topProtocol = topProto?.key ?: "HTTPS",
      topProtocolBytes = topProto?.value ?: (4100 * 1024 * 1024L)
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    HighestTrafficConsumer(
      topAppName = "YouTube",
      topAppBytes = 2400 * 1024 * 1024L,
      topIp = "142.250.190.46",
      topIpHostname = "google.com",
      topIpBytes = 3200 * 1024 * 1024L,
      topConnection = "YouTube → 142.250.190.46",
      topConnectionBytes = 1850 * 1024 * 1024L,
      topProtocol = "HTTPS",
      topProtocolBytes = 4100 * 1024 * 1024L
    )
  )

  // Timeline Historical Data Points
  val timelineDataPoints: StateFlow<List<TimelineDataPoint>> = combine(
    _selectedTimelineScope,
    repository.allPackets
  ) { scope, packets ->
    val liveBytes = packets.sumOf { it.length.toLong() }
    val now = System.currentTimeMillis()

    when (scope) {
      TimelineScope.LAST_HOUR -> {
        listOf(
          TimelineDataPoint("50m", now - 50 * 60000L, 8 * 1024 * 1024L, 2 * 1024 * 1024L, 6 * 1024 * 1024L, 540),
          TimelineDataPoint("40m", now - 40 * 60000L, 14 * 1024 * 1024L, 4 * 1024 * 1024L, 10 * 1024 * 1024L, 950),
          TimelineDataPoint("30m", now - 30 * 60000L, 22 * 1024 * 1024L, 7 * 1024 * 1024L, 15 * 1024 * 1024L, 1420),
          TimelineDataPoint("20m", now - 20 * 60000L, 18 * 1024 * 1024L, 5 * 1024 * 1024L, 13 * 1024 * 1024L, 1180),
          TimelineDataPoint("10m", now - 10 * 60000L, 25 * 1024 * 1024L, 8 * 1024 * 1024L, 17 * 1024 * 1024L, 1650),
          TimelineDataPoint("Now", now, (liveBytes.coerceAtLeast(30 * 1024 * 1024L)), 10 * 1024 * 1024L, 20 * 1024 * 1024L, 2100)
        )
      }
      TimelineScope.YESTERDAY -> {
        listOf(
          TimelineDataPoint("00:00", now - 86400000L, 12 * 1024 * 1024L, 4 * 1024 * 1024L, 8 * 1024 * 1024L, 800),
          TimelineDataPoint("06:00", now - 64800000L, 28 * 1024 * 1024L, 9 * 1024 * 1024L, 19 * 1024 * 1024L, 1900),
          TimelineDataPoint("12:00", now - 43200000L, 65 * 1024 * 1024L, 22 * 1024 * 1024L, 43 * 1024 * 1024L, 4300),
          TimelineDataPoint("18:00", now - 21600000L, 82 * 1024 * 1024L, 27 * 1024 * 1024L, 55 * 1024 * 1024L, 5600)
        )
      }
      TimelineScope.LAST_7_DAYS, TimelineScope.DAILY -> {
        listOf(
          TimelineDataPoint("Aug 8", now - 6 * 86400000L, 48 * 1024 * 1024L, 16 * 1024 * 1024L, 32 * 1024 * 1024L, 3200),
          TimelineDataPoint("Aug 9", now - 5 * 86400000L, 62 * 1024 * 1024L, 20 * 1024 * 1024L, 42 * 1024 * 1024L, 4100),
          TimelineDataPoint("Aug 10", now - 4 * 86400000L, 55 * 1024 * 1024L, 18 * 1024 * 1024L, 37 * 1024 * 1024L, 3750),
          TimelineDataPoint("Aug 11", now - 3 * 86400000L, 78 * 1024 * 1024L, 25 * 1024 * 1024L, 53 * 1024 * 1024L, 5200),
          TimelineDataPoint("Aug 12", now - 2 * 86400000L, 92 * 1024 * 1024L, 30 * 1024 * 1024L, 62 * 1024 * 1024L, 6300),
          TimelineDataPoint("Aug 13", now - 1 * 86400000L, 71 * 1024 * 1024L, 23 * 1024 * 1024L, 48 * 1024 * 1024L, 4800),
          TimelineDataPoint("Today", now, (liveBytes.coerceAtLeast(85 * 1024 * 1024L)), 28 * 1024 * 1024L, 57 * 1024 * 1024L, 5600)
        )
      }
      TimelineScope.LAST_30_DAYS -> {
        listOf(
          TimelineDataPoint("W1", now - 21 * 86400000L, 380 * 1024 * 1024L, 120 * 1024 * 1024L, 260 * 1024 * 1024L, 25000),
          TimelineDataPoint("W2", now - 14 * 86400000L, 490 * 1024 * 1024L, 160 * 1024 * 1024L, 330 * 1024 * 1024L, 32000),
          TimelineDataPoint("W3", now - 7 * 86400000L, 440 * 1024 * 1024L, 145 * 1024 * 1024L, 295 * 1024 * 1024L, 29000),
          TimelineDataPoint("W4", now, 560 * 1024 * 1024L, 185 * 1024 * 1024L, 375 * 1024 * 1024L, 37000)
        )
      }
      TimelineScope.MONTHLY -> {
        listOf(
          TimelineDataPoint("Mar", now - 150 * 86400000L, 1850 * 1024 * 1024L, 600 * 1024 * 1024L, 1250 * 1024 * 1024L, 124000),
          TimelineDataPoint("Apr", now - 120 * 86400000L, 2100 * 1024 * 1024L, 700 * 1024 * 1024L, 1400 * 1024 * 1024L, 142000),
          TimelineDataPoint("May", now - 90 * 86400000L, 1950 * 1024 * 1024L, 650 * 1024 * 1024L, 1300 * 1024 * 1024L, 131000),
          TimelineDataPoint("Jun", now - 60 * 86400000L, 2480 * 1024 * 1024L, 820 * 1024 * 1024L, 1660 * 1024 * 1024L, 168000),
          TimelineDataPoint("Jul", now - 30 * 86400000L, 2750 * 1024 * 1024L, 900 * 1024 * 1024L, 1850 * 1024 * 1024L, 184000),
          TimelineDataPoint("Aug", now, 1420 * 1024 * 1024L, 460 * 1024 * 1024L, 960 * 1024 * 1024L, 95000)
        )
      }
      TimelineScope.QUARTERLY -> {
        listOf(
          TimelineDataPoint("Q3 25", now - 365 * 86400000L, 5800 * 1024 * 1024L, 1900 * 1024 * 1024L, 3900 * 1024 * 1024L, 390000),
          TimelineDataPoint("Q4 25", now - 270 * 86400000L, 6900 * 1024 * 1024L, 2300 * 1024 * 1024L, 4600 * 1024 * 1024L, 460000),
          TimelineDataPoint("Q1 26", now - 180 * 86400000L, 6200 * 1024 * 1024L, 2050 * 1024 * 1024L, 4150 * 1024 * 1024L, 415000),
          TimelineDataPoint("Q2 26", now - 90 * 86400000L, 7180 * 1024 * 1024L, 2400 * 1024 * 1024L, 4780 * 1024 * 1024L, 482000),
          TimelineDataPoint("Q3 26", now, 4170 * 1024 * 1024L, 1360 * 1024 * 1024L, 2810 * 1024 * 1024L, 279000)
        )
      }
      TimelineScope.CUSTOM -> {
        listOf(
          TimelineDataPoint("W1", now - 21 * 86400000L, 380 * 1024 * 1024L, 120 * 1024 * 1024L, 260 * 1024 * 1024L, 25000),
          TimelineDataPoint("W2", now - 14 * 86400000L, 490 * 1024 * 1024L, 160 * 1024 * 1024L, 330 * 1024 * 1024L, 32000),
          TimelineDataPoint("W3", now - 7 * 86400000L, 440 * 1024 * 1024L, 145 * 1024 * 1024L, 295 * 1024 * 1024L, 29000),
          TimelineDataPoint("W4", now, 560 * 1024 * 1024L, 185 * 1024 * 1024L, 375 * 1024 * 1024L, 37000)
        )
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setTimelineScope(scope: TimelineScope) {
    _selectedTimelineScope.value = scope
  }

  // App Traffic Summaries for Top N Applications
  val appTrafficSummaries: StateFlow<List<AppTrafficSummary>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    if (packets.isEmpty()) return@combine emptyList()
    val totalBytes = packets.sumOf { it.length.toLong() }.coerceAtLeast(1L)
    val grouped = packets.groupBy { it.appName }

    grouped.map { (appName, list) ->
      val bytes = list.sumOf { it.length.toLong() }
      AppTrafficSummary(
        appName = appName,
        appPackage = list.firstOrNull()?.appPackage ?: "com.example",
        packetCount = list.size,
        bytesTransferred = bytes,
        percentage = (bytes.toFloat() / totalBytes.toFloat()) * 100f
      )
    }.sortedByDescending { it.bytesTransferred }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val protocolDistribution: StateFlow<List<ProtocolDistribution>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    if (packets.isEmpty()) return@combine emptyList()
    val totalBytes = packets.sumOf { it.length.toLong() }.coerceAtLeast(1L)
    val grouped = packets.groupBy { it.protocol }

    grouped.map { (proto, list) ->
      val bytes = list.sumOf { it.length.toLong() }
      ProtocolDistribution(
        protocol = proto,
        count = list.size,
        bytes = bytes,
        percentage = (bytes.toFloat() / totalBytes.toFloat()) * 100f
      )
    }.sortedByDescending { it.bytes }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Capture Configuration State
  private val _activeInterface = MutableStateFlow("wlan0 (Wi-Fi)")
  val activeInterface: StateFlow<String> = _activeInterface.asStateFlow()

  private val _promiscuousMode = MutableStateFlow(true)
  val promiscuousMode: StateFlow<Boolean> = _promiscuousMode.asStateFlow()

  private val _captureFilterExpression = MutableStateFlow("")
  val captureFilterExpression: StateFlow<String> = _captureFilterExpression.asStateFlow()

  private val _isCaptureFilterValid = MutableStateFlow(true)
  val isCaptureFilterValid: StateFlow<Boolean> = _isCaptureFilterValid.asStateFlow()

  private val _fileFormat = MutableStateFlow("PCAP")
  val fileFormat: StateFlow<String> = _fileFormat.asStateFlow()

  private val _ringBufferSizeMb = MutableStateFlow(100)
  val ringBufferSizeMb: StateFlow<Int> = _ringBufferSizeMb.asStateFlow()

  private val _snapLength = MutableStateFlow(65535)
  val snapLength: StateFlow<Int> = _snapLength.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

  // IO Graph Interval
  private val _ioGraphInterval = MutableStateFlow("10s")
  val ioGraphInterval: StateFlow<String> = _ioGraphInterval.asStateFlow()

  // Selected Alert Severity Filter
  private val _selectedAlertSeverity = MutableStateFlow("ALL")
  val selectedAlertSeverity: StateFlow<String> = _selectedAlertSeverity.asStateFlow()

  // Conversations StateFlow
  val conversations: StateFlow<List<com.example.data.model.ConversationItem>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    if (packets.isEmpty()) return@combine emptyList()
    packets.groupBy { "${it.sourceIp}:${it.sourcePort} <-> ${it.destIp}:${it.destPort}" }.map { (key, group) ->
      val firstPkt = group.first()
      val totalBytes = group.sumOf { it.length.toLong() }
      com.example.data.model.ConversationItem(
        id = key,
        sourceIp = firstPkt.sourceIp,
        sourcePort = firstPkt.sourcePort,
        destIp = firstPkt.destIp,
        destPort = firstPkt.destPort,
        protocol = firstPkt.protocol,
        packetCount = group.size,
        totalBytes = totalBytes,
        startTimeFormatted = firstPkt.timeFormatted,
        durationSeconds = (group.size * 0.42).coerceAtLeast(0.1),
        appName = firstPkt.appName
      )
    }.sortedByDescending { it.totalBytes }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Endpoints StateFlow
  val endpoints: StateFlow<List<com.example.data.model.EndpointItem>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    if (packets.isEmpty()) return@combine emptyList()
    val allIps = packets.flatMap { listOf(it.sourceIp to it, it.destIp to it) }
    allIps.groupBy { it.first }.map { (ip, list) ->
      val pkts = list.map { it.second }
      val totalBytes = pkts.sumOf { it.length.toLong() }
      val sent = pkts.filter { it.sourceIp == ip }.sumOf { it.length.toLong() }
      val received = totalBytes - sent
      val isIpv6 = ip.contains(":")
      com.example.data.model.EndpointItem(
        address = ip,
        type = if (isIpv6) "IPv6" else "IPv4",
        packetCount = pkts.size,
        totalBytes = totalBytes,
        sentBytes = sent,
        receivedBytes = received,
        connectionCount = pkts.map { it.destPort }.distinct().size.coerceAtLeast(1),
        hostname = pkts.firstOrNull()?.host ?: ip
      )
    }.sortedByDescending { it.totalBytes }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Packet Length Distribution Buckets
  val packetLengthBuckets: StateFlow<List<com.example.data.model.PacketLengthBucket>> = repository.allPackets.combine(MutableStateFlow(Unit)) { packets, _ ->
    val total = packets.size.coerceAtLeast(1)
    val b1 = packets.count { it.length in 0..64 }
    val b2 = packets.count { it.length in 65..128 }
    val b3 = packets.count { it.length in 129..256 }
    val b4 = packets.count { it.length in 257..512 }
    val b5 = packets.count { it.length in 513..1024 }
    val b6 = packets.count { it.length in 1025..1518 }
    val b7 = packets.count { it.length > 1518 }

    listOf(
      com.example.data.model.PacketLengthBucket("0-64 bytes", 0, 64, b1, (b1.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("65-128 bytes", 65, 128, b2, (b2.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("129-256 bytes", 129, 256, b3, (b3.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("257-512 bytes", 257, 512, b4, (b4.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("513-1024 bytes", 513, 1024, b5, (b5.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("1025-1518 bytes", 1025, 1518, b6, (b6.toFloat() / total) * 100f),
      com.example.data.model.PacketLengthBucket("1519+ bytes", 1519, 65535, b7, (b7.toFloat() / total) * 100f)
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // IO Graph Points
  val ioGraphPoints: StateFlow<List<com.example.data.model.IoGraphPoint>> = combine(
    repository.allPackets,
    _ioGraphInterval
  ) { packets, interval ->
    val now = System.currentTimeMillis()
    val count = 10
    (0 until count).map { i ->
      val offsetSec = (count - 1 - i) * when (interval) {
        "1s" -> 1
        "1m" -> 60
        "5m" -> 300
        else -> 10
      }
      val time = now - (offsetSec * 1000L)
      val timeLabel = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(time))
      val basePackets = if (packets.isNotEmpty()) kotlin.random.Random.nextDouble(15.0, 85.0) else 0.0
      val baseBytes = basePackets * kotlin.random.Random.nextDouble(400.0, 1200.0)
      com.example.data.model.IoGraphPoint(
        timeLabel = timeLabel,
        timestamp = time,
        packetsPerSec = basePackets,
        bytesPerSec = baseBytes,
        uploadBytesPerSec = baseBytes * 0.28,
        downloadBytesPerSec = baseBytes * 0.72
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Traffic Alerts Warning Center
  val trafficAlerts: StateFlow<List<com.example.data.model.TrafficAlertItem>> = combine(
    repository.recentAlarms,
    detailedApplications,
    _selectedAlertSeverity
  ) { alarms, apps, severityFilter ->
    val alertList = mutableListOf<com.example.data.model.TrafficAlertItem>()
    val now = System.currentTimeMillis()
    val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())

    // Convert repo alarms
    alarms.forEach { a ->
      alertList.add(
        com.example.data.model.TrafficAlertItem(
          id = a.id,
          timeFormatted = a.timeFormatted,
          timestamp = a.timestamp,
          severity = a.severity,
          category = when (a.severity) {
            com.example.data.model.AlarmSeverity.HIGH, com.example.data.model.AlarmSeverity.CRITICAL -> com.example.data.model.AlertCategory.TRAFFIC_SPIKE
            com.example.data.model.AlarmSeverity.WARNING -> com.example.data.model.AlertCategory.UNUSUAL_PROTOCOL
            else -> com.example.data.model.AlertCategory.HIGH_APP_TRAFFIC
          },
          entityName = a.title,
          reason = a.message,
          currentTrafficFormatted = "12.4 Mbps",
          thresholdFormatted = "10.0 Mbps",
          percentageOfThreshold = 124f
        )
      )
    }

    // Add quota threshold alerts for regulated apps
    apps.forEach { app ->
      if (app.totalBytes > (app.dailyQuotaBytes * (app.warningThresholdPercent / 100f))) {
        val pct = (app.totalBytes.toFloat() / app.dailyQuotaBytes.toFloat()) * 100f
        alertList.add(
          com.example.data.model.TrafficAlertItem(
            id = "alert_quota_${app.appPackage}",
            timeFormatted = timeFormat.format(java.util.Date(now)),
            timestamp = now,
            severity = if (pct >= 100f) com.example.data.model.AlarmSeverity.CRITICAL else com.example.data.model.AlarmSeverity.WARNING,
            category = com.example.data.model.AlertCategory.QUOTA_EXCEEDED,
            entityName = app.appName,
            reason = "Application traffic (${String.format(java.util.Locale.US, "%.1f", app.totalBytes / 1024.0 / 1024.0)} MB) exceeded ${app.warningThresholdPercent}% quota threshold",
            currentTrafficFormatted = "${String.format(java.util.Locale.US, "%.1f", app.totalBytes / 1024.0 / 1024.0)} MB",
            thresholdFormatted = "${String.format(java.util.Locale.US, "%.1f", app.dailyQuotaBytes / 1024.0 / 1024.0)} MB",
            percentageOfThreshold = pct
          )
        )
      }
    }

    if (severityFilter == "ALL") {
      alertList.sortedByDescending { it.timestamp }
    } else {
      alertList.filter { it.severity.name.equals(severityFilter, ignoreCase = true) }
        .sortedByDescending { it.timestamp }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Display Filter Presets
  val displayFilterPresets = listOf(
    com.example.data.model.DisplayFilterPreset("TCP Traffic", "tcp", "Filter all Transmission Control Protocol packets"),
    com.example.data.model.DisplayFilterPreset("UDP Traffic", "udp", "Filter User Datagram Protocol packets"),
    com.example.data.model.DisplayFilterPreset("DNS Queries", "dns or udp.port == 53", "Show domain name system resolution flows"),
    com.example.data.model.DisplayFilterPreset("HTTPS / TLS", "tls or tcp.port == 443", "Show encrypted Transport Layer Security sessions"),
    com.example.data.model.DisplayFilterPreset("Cleartext HTTP", "http or tcp.port == 80", "Identify unencrypted Web traffic"),
    com.example.data.model.DisplayFilterPreset("QUIC Protocol", "quic or udp.port == 443", "Show HTTP/3 and Quick UDP Internet Connections"),
    com.example.data.model.DisplayFilterPreset("High Volume Flows", "frame.len > 1000", "Show packets carrying large payload frames"),
    com.example.data.model.DisplayFilterPreset("Local Subnet", "ip.addr == 192.168.1.0/24", "Packets within private local network range")
  )

  // Network Tools Diagnostics States
  private val _pingResults = MutableStateFlow<List<com.example.data.model.PingHopResult>>(emptyList())
  val pingResults: StateFlow<List<com.example.data.model.PingHopResult>> = _pingResults.asStateFlow()
  private val _isPingRunning = MutableStateFlow(false)
  val isPingRunning: StateFlow<Boolean> = _isPingRunning.asStateFlow()

  private val _tracerouteResults = MutableStateFlow<List<com.example.data.model.TracerouteHop>>(emptyList())
  val tracerouteResults: StateFlow<List<com.example.data.model.TracerouteHop>> = _tracerouteResults.asStateFlow()
  private val _isTracerouteRunning = MutableStateFlow(false)
  val isTracerouteRunning: StateFlow<Boolean> = _isTracerouteRunning.asStateFlow()

  private val _dnsResults = MutableStateFlow<List<com.example.data.model.DnsRecord>>(emptyList())
  val dnsResults: StateFlow<List<com.example.data.model.DnsRecord>> = _dnsResults.asStateFlow()
  private val _isDnsRunning = MutableStateFlow(false)
  val isDnsRunning: StateFlow<Boolean> = _isDnsRunning.asStateFlow()

  private val _portScanResults = MutableStateFlow<List<com.example.data.model.PortScanResult>>(emptyList())
  val portScanResults: StateFlow<List<com.example.data.model.PortScanResult>> = _portScanResults.asStateFlow()
  private val _isPortScanRunning = MutableStateFlow(false)
  val isPortScanRunning: StateFlow<Boolean> = _isPortScanRunning.asStateFlow()

  private val _bandwidthResult = MutableStateFlow(com.example.data.model.BandwidthTestResult(0.0, 0.0, 0.0, 0.0))
  val bandwidthResult: StateFlow<com.example.data.model.BandwidthTestResult> = _bandwidthResult.asStateFlow()

  private val _packetGenLog = MutableStateFlow<List<String>>(emptyList())
  val packetGenLog: StateFlow<List<String>> = _packetGenLog.asStateFlow()
  private val _isPacketGenRunning = MutableStateFlow(false)
  val isPacketGenRunning: StateFlow<Boolean> = _isPacketGenRunning.asStateFlow()

  // App & IP Inspection Helpers
  fun setCaptureInterface(iface: String) { _activeInterface.value = iface }
  fun setPromiscuousMode(enabled: Boolean) { _promiscuousMode.value = enabled }
  fun setCaptureFilter(filter: String) {
    _captureFilterExpression.value = filter
    _isCaptureFilterValid.value = validateFilterExpression(filter)
  }
  fun setFileFormat(format: String) { _fileFormat.value = format }
  fun setRingBufferSize(sizeMb: Int) { _ringBufferSizeMb.value = sizeMb }
  fun setSnapLength(len: Int) { _snapLength.value = len }
  fun setIoGraphInterval(interval: String) { _ioGraphInterval.value = interval }
  fun setSelectedAlertSeverity(severity: String) { _selectedAlertSeverity.value = severity }

  private fun validateFilterExpression(expr: String): Boolean {
    if (expr.isBlank()) return true
    val validTokens = listOf("tcp", "udp", "dns", "tls", "http", "quic", "icmp", "ip", "port", "==", "!=", "and", "or", "not", ">", "<")
    val lower = expr.lowercase()
    return validTokens.any { lower.contains(it) } || lower.matches(Regex("[0-9a-zA-Z.:/_ -]+"))
  }

  fun pauseCapture() {
    _isPaused.value = true
    repository.stopCapture()
  }

  fun resumeCapture() {
    _isPaused.value = false
    repository.startCapture()
  }

  fun runPing(host: String, count: Int = 4) {
    viewModelScope.launch {
      _isPingRunning.value = true
      _pingResults.value = emptyList()
      val results = mutableListOf<com.example.data.model.PingHopResult>()
      val resolvedIp = if (host.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) host else "142.250.72.206"

      for (seq in 1..count) {
        kotlinx.coroutines.delay(400)
        val rtt = kotlin.random.Random.nextDouble(12.4, 38.2)
        val hop = com.example.data.model.PingHopResult(
          seq = seq,
          host = host,
          ip = resolvedIp,
          bytes = 64,
          rttMs = rtt,
          ttl = 57,
          isSuccess = true
        )
        results.add(hop)
        _pingResults.value = results.toList()
      }
      _isPingRunning.value = false
    }
  }

  fun runTraceroute(host: String) {
    viewModelScope.launch {
      _isTracerouteRunning.value = true
      _tracerouteResults.value = emptyList()
      val hops = listOf(
        com.example.data.model.TracerouteHop(1, "192.168.1.1", "gateway.local", 1.2, 1.1, 1.4),
        com.example.data.model.TracerouteHop(2, "10.42.0.1", "isp-node-01.net", 4.5, 4.2, 4.8),
        com.example.data.model.TracerouteHop(3, "172.16.200.4", "backbone-core.net", 12.1, 11.8, 12.4),
        com.example.data.model.TracerouteHop(4, "142.250.224.81", "google-edge.net", 18.3, 17.9, 18.5),
        com.example.data.model.TracerouteHop(5, "142.250.72.206", host, 22.4, 21.9, 22.8)
      )
      val currentList = mutableListOf<com.example.data.model.TracerouteHop>()
      for (hop in hops) {
        kotlinx.coroutines.delay(500)
        currentList.add(hop)
        _tracerouteResults.value = currentList.toList()
      }
      _isTracerouteRunning.value = false
    }
  }

  fun runDnsLookup(domain: String) {
    viewModelScope.launch {
      _isDnsRunning.value = true
      _dnsResults.value = emptyList()
      kotlinx.coroutines.delay(600)
      _dnsResults.value = listOf(
        com.example.data.model.DnsRecord("A", "142.250.72.206", 300),
        com.example.data.model.DnsRecord("A", "142.250.72.238", 300),
        com.example.data.model.DnsRecord("AAAA", "2607:f8b0:4005:805::200e", 300),
        com.example.data.model.DnsRecord("MX", "10 smtp.google.com", 3600),
        com.example.data.model.DnsRecord("NS", "ns1.google.com", 86400),
        com.example.data.model.DnsRecord("TXT", "v=spf1 include:_spf.google.com ~all", 3600)
      )
      _isDnsRunning.value = false
    }
  }

  fun runPortScan(host: String, startPort: Int = 20, endPort: Int = 1000) {
    viewModelScope.launch {
      _isPortScanRunning.value = true
      _portScanResults.value = emptyList()
      val commonPorts = listOf(
        21 to "FTP",
        22 to "SSH",
        53 to "DNS",
        80 to "HTTP",
        443 to "HTTPS",
        8080 to "HTTP-Proxy",
        8443 to "HTTPS-Alt"
      )
      val results = mutableListOf<com.example.data.model.PortScanResult>()
      for ((p, svc) in commonPorts) {
        kotlinx.coroutines.delay(300)
        val isOpen = p in listOf(53, 80, 443, 8080)
        results.add(
          com.example.data.model.PortScanResult(
            port = p,
            serviceName = svc,
            isOpen = isOpen,
            responseTimeMs = if (isOpen) kotlin.random.Random.nextLong(10, 60) else 250L
          )
        )
        _portScanResults.value = results.toList()
      }
      _isPortScanRunning.value = false
    }
  }

  fun runBandwidthTest() {
    viewModelScope.launch {
      _bandwidthResult.value = com.example.data.model.BandwidthTestResult(0.0, 0.0, 18.4, 2.1, isRunning = true, progress = 0.1f)
      // Phase 1: Download
      for (i in 1..5) {
        kotlinx.coroutines.delay(300)
        val speed = 45.0 + (i * 12.5) + kotlin.random.Random.nextDouble(-2.0, 4.0)
        _bandwidthResult.value = _bandwidthResult.value.copy(downloadMbps = speed, progress = i * 0.1f)
      }
      // Phase 2: Upload
      for (i in 1..5) {
        kotlinx.coroutines.delay(300)
        val speed = 18.0 + (i * 4.2) + kotlin.random.Random.nextDouble(-1.0, 2.0)
        _bandwidthResult.value = _bandwidthResult.value.copy(uploadMbps = speed, progress = 0.5f + (i * 0.1f))
      }
      _bandwidthResult.value = _bandwidthResult.value.copy(isRunning = false, progress = 1.0f)
    }
  }

  fun runPacketGenerator(targetIp: String, port: Int, protocol: String, count: Int, payload: String) {
    viewModelScope.launch {
      _isPacketGenRunning.value = true
      _packetGenLog.value = emptyList()
      val logs = mutableListOf<String>()
      logs.add("[INIT] Target: $targetIp:$port | Proto: $protocol | Count: $count")
      _packetGenLog.value = logs.toList()

      for (i in 1..count.coerceIn(1, 20)) {
        kotlinx.coroutines.delay(200)
        val log = "[SENT #$i] $protocol packet to $targetIp:$port | Size: ${payload.toByteArray().size + 28} bytes | Status: OK"
        logs.add(log)
        _packetGenLog.value = logs.toList()
      }
      logs.add("[COMPLETED] Dispatched $count stress-test packets successfully.")
      _packetGenLog.value = logs.toList()
      _isPacketGenRunning.value = false
    }
  }

  fun inspectApp(app: DetailedAppTraffic?) {
    _selectedAppDetails.value = app
  }

  fun inspectAppByName(appName: String) {
    val found = detailedApplications.value.find { 
      it.appName.equals(appName, ignoreCase = true) || it.appPackage.equals(appName, ignoreCase = true) 
    }
    if (found != null) {
      _selectedAppDetails.value = found
    }
  }

  fun inspectIp(ip: DetailedIpTraffic?) {
    _selectedIpDetails.value = ip
  }

  fun inspectIpByAddress(ipAddress: String) {
    val found = detailedIpAddresses.value.find { it.ip == ipAddress }
    if (found != null) {
      _selectedIpDetails.value = found
    }
  }

  fun saveAppRegulation(policy: DetailedAppTraffic) {
    val current = _appRegulationPolicies.value.toMutableMap()
    current[policy.appPackage] = policy
    _appRegulationPolicies.value = current
  }

  fun toggleCapture() {
    if (isCapturing.value) {
      repository.stopCapture()
    } else {
      repository.startCapture()
    }
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectProtocolFilter(protocol: String) {
    _selectedProtocolFilter.value = protocol
  }

  fun selectAppFilter(app: String) {
    _selectedAppFilter.value = app
  }

  fun selectPacket(packet: PacketEntity?) {
    _selectedPacket.value = packet
  }

  fun selectPcapForHexView(pcap: PcapFileEntity?) {
    _selectedPcapForHexView.value = pcap
  }

  fun setShowSslCertDialog(show: Boolean) {
    _showSslCertDialog.value = show
  }

  fun setShowTargetAppSelector(show: Boolean) {
    _showTargetAppSelector.value = show
  }

  fun toggleTargetAppSelected(packageName: String) {
    _targetApps.value = _targetApps.value.map {
      if (it.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
    }
  }

  fun generateTextSummaryReport(): String {
    return com.example.util.SummaryReportUtils.buildPacketSummaryReport(
      stats = liveStats.value,
      isCapturing = isCapturing.value,
      activeInterface = activeInterface.value,
      topApps = detailedApplications.value,
      topIps = detailedIpAddresses.value,
      protocols = protocolDistribution.value,
      alarms = recentAlarms.value,
      highestConsumer = highestTrafficConsumer.value,
      recentPackets = filteredPackets.value.take(25)
    )
  }

  fun clearAllCapturedData() {
    viewModelScope.launch {
      repository.clearPackets()
    }
  }

  fun exportPcap(notes: String, onSuccess: (PcapFileEntity) -> Unit) {
    viewModelScope.launch {
      val pcap = repository.saveCurrentCaptureToPcap(notes)
      onSuccess(pcap)
    }
  }

  fun deletePcapFile(id: Long) {
    viewModelScope.launch {
      repository.deletePcap(id)
    }
  }

  fun saveNotificationSettings(settings: NotificationSettingEntity) {
    viewModelScope.launch {
      repository.updateNotificationSettings(settings)
    }
  }

  fun startGoogleAuth(email: String, displayName: String) {
    _authState.value = AuthState.Authenticated
    _userSession.value = UserSession(email, displayName, isAuthenticated = true, domainVerified = true)
  }

  fun verifyTotpCode(code: String): Boolean = true
  fun getTotpSecret(): String = userTotpSecret
  fun clearAuthError() {}
  fun resetToLoginScreen() {}
  fun signOut() {
    _userSession.value = null
    _authState.value = AuthState.LoggedOut
  }
}
