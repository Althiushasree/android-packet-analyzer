package com.example.data.intelligence

import android.content.Context
import com.example.data.model.AuthorizedNetworkScope
import com.example.data.model.NetworkScopeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service managing real-time network traffic & device monitoring.
 * Strictly operates on REAL observed network data. Zero mock/demo data.
 */
class NetworkMonitoringService(
  private val context: Context,
  private val intelligenceManager: NetworkIntelligenceManager
) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

  private val _networkScopes = MutableStateFlow<List<AuthorizedNetworkScope>>(emptyList())
  val networkScopes: StateFlow<List<AuthorizedNetworkScope>> = _networkScopes.asStateFlow()

  private val _selectedScope = MutableStateFlow<AuthorizedNetworkScope>(
    NetworkScopeFactory.fromLiveNetwork(
      intelligenceManager.networkInfo.value,
      activeDevicesCount = intelligenceManager.observedDevices.value.count { it.isActive },
      totalDevicesCount = intelligenceManager.observedDevices.value.size
    )
  )
  val selectedScope: StateFlow<AuthorizedNetworkScope> = _selectedScope.asStateFlow()

  private val _scopedDevices = MutableStateFlow<List<ObservedNetworkDevice>>(emptyList())
  val scopedDevices: StateFlow<List<ObservedNetworkDevice>> = _scopedDevices.asStateFlow()

  private val _scopedFlows = MutableStateFlow<List<CommunicationFlow>>(emptyList())
  val scopedFlows: StateFlow<List<CommunicationFlow>> = _scopedFlows.asStateFlow()

  private val _scopedServices = MutableStateFlow<List<ApplicationServiceAnalysis>>(emptyList())
  val scopedServices: StateFlow<List<ApplicationServiceAnalysis>> = _scopedServices.asStateFlow()

  private val _scopedAlerts = MutableStateFlow<List<DefensiveSecurityAlert>>(emptyList())
  val scopedAlerts: StateFlow<List<DefensiveSecurityAlert>> = _scopedAlerts.asStateFlow()

  init {
    // Continuously observe real live network changes from NetworkIntelligenceManager
    scope.launch {
      intelligenceManager.networkInfo.collectLatest { netInfo ->
        val devCount = intelligenceManager.observedDevices.value.size
        val activeCount = intelligenceManager.observedDevices.value.count { it.isActive }
        val liveScope = NetworkScopeFactory.fromLiveNetwork(
          liveInfo = netInfo,
          activeDevicesCount = activeCount,
          totalDevicesCount = devCount
        )

        val currentList = _networkScopes.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == liveScope.id }
        if (existingIndex >= 0) {
          currentList[existingIndex] = liveScope
        } else {
          currentList.add(0, liveScope)
        }
        _networkScopes.value = currentList

        if (_selectedScope.value.isLive || _selectedScope.value.id == liveScope.id) {
          _selectedScope.value = liveScope
        }
      }
    }

    // Continuously observe real devices
    scope.launch {
      intelligenceManager.observedDevices.collectLatest { devices ->
        if (_selectedScope.value.isLive) {
          _scopedDevices.value = devices
        }
      }
    }

    // Continuously observe real flows
    scope.launch {
      intelligenceManager.communicationFlows.collectLatest { flows ->
        if (_selectedScope.value.isLive) {
          _scopedFlows.value = flows
        }
      }
    }

    // Continuously observe real services
    scope.launch {
      intelligenceManager.applicationServices.collectLatest { services ->
        if (_selectedScope.value.isLive) {
          _scopedServices.value = services
        }
      }
    }

    // Continuously observe real alerts
    scope.launch {
      intelligenceManager.securityAlerts.collectLatest { alerts ->
        if (_selectedScope.value.isLive) {
          _scopedAlerts.value = alerts
        }
      }
    }

    // Load real historical network sessions from Room database
    scope.launch {
      intelligenceManager.dbManager.allSessions.collectLatest { sessions ->
        val distinctNetworks = sessions.groupBy { it.networkName }
        val historyScopes = distinctNetworks.map { (netName, sessList) ->
          val latest = sessList.maxByOrNull { it.startTime } ?: sessList.first()
          val totalBytes = sessList.sumOf { it.totalBytes.toLong() }
          val totalUpload = sessList.sumOf { it.uploadBytes.toLong() }
          val totalDownload = sessList.sumOf { it.downloadBytes.toLong() }

          AuthorizedNetworkScope(
            id = "HISTORY-${netName.replace(" ", "_")}-${latest.sessionId}",
            name = "Recorded Network: $netName",
            ssid = netName,
            subnet = latest.subnet,
            cidr = "/24",
            gatewayIp = latest.gateway,
            dhcpServer = "${latest.gateway} (Recorded Gateway)",
            dnsServers = latest.dnsServers.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            networkType = "${latest.interfaceType} (${latest.interfaceName})",
            monitoringInterface = latest.interfaceName,
            monitoringSource = "Room Database Network Session (${latest.sessionId})",
            isLive = false,
            activeDevicesCount = 0,
            totalDiscoveredCount = 1,
            totalTrafficBytes = totalBytes,
            totalUploadBytes = totalUpload,
            totalDownloadBytes = totalDownload,
            networkVisibilityMode = "LOCAL_DEVICE_ONLY",
            visibilityExplanation = "Historical Session Data recorded at ${dateFormat.format(Date(latest.startTime))}."
          )
        }

        val liveScope = NetworkScopeFactory.fromLiveNetwork(
          intelligenceManager.networkInfo.value,
          activeDevicesCount = intelligenceManager.observedDevices.value.count { it.isActive },
          totalDevicesCount = intelligenceManager.observedDevices.value.size
        )

        val updatedList = mutableListOf(liveScope)
        updatedList.addAll(historyScopes.filter { it.ssid != liveScope.ssid })
        _networkScopes.value = updatedList
      }
    }
  }

  fun selectNetworkScope(scopeId: String) {
    val found = _networkScopes.value.firstOrNull { it.id == scopeId } ?: return
    _selectedScope.value = found

    if (found.isLive) {
      _scopedDevices.value = intelligenceManager.observedDevices.value
      _scopedFlows.value = intelligenceManager.communicationFlows.value
      _scopedServices.value = intelligenceManager.applicationServices.value
      _scopedAlerts.value = intelligenceManager.securityAlerts.value
    } else {
      // Historical or collector scope: query real database records for this network
      scope.launch {
        intelligenceManager.dbManager.deviceDao.getAllDevicesFlow().collectLatest { dbDevices ->
          val mapped = dbDevices.map { d ->
            ObservedNetworkDevice(
              id = d.ipAddress,
              ipAddress = d.ipAddress,
              macAddress = d.macAddress,
              hostname = d.hostname,
              vendor = d.vendor,
              estimatedDeviceType = when (d.deviceType) {
                "GATEWAY", "ROUTER" -> DeviceType.GATEWAY
                "LAPTOP" -> DeviceType.LAPTOP
                "SMARTPHONE" -> DeviceType.SMARTPHONE
                "SERVER" -> DeviceType.SERVER
                "PRINTER" -> DeviceType.PRINTER
                else -> DeviceType.UNKNOWN
              },
              isGateway = d.deviceType == "GATEWAY" || d.deviceType == "ROUTER",
              isLocalHost = d.deviceType == "LOCAL_DEVICE",
              firstSeenTimestamp = d.firstSeen,
              lastSeenTimestamp = d.lastSeen,
              firstSeenFormatted = dateFormat.format(Date(d.firstSeen)),
              lastSeenFormatted = dateFormat.format(Date(d.lastSeen)),
              isActive = false, // Historical device session
              totalBytes = 0L,
              totalPackets = 0L,
              uploadBytes = 0L,
              downloadBytes = 0L,
              activeConnectionsCount = 0,
              confidence = "Persisted Room Database Record"
            )
          }
          _scopedDevices.value = mapped
        }
      }
    }
  }

  /**
   * Generates real audit report for export based strictly on real observed telemetry.
   */
  fun generateReport(format: String, target: String, timeRange: String): String {
    val scopeObj = _selectedScope.value
    val devices = _scopedDevices.value
    val flows = _scopedFlows.value
    val services = _scopedServices.value
    val alerts = _scopedAlerts.value

    return when (format.uppercase()) {
      "JSON" -> buildJsonReport(scopeObj, devices, flows, services, alerts, timeRange)
      "CSV" -> buildCsvReport(scopeObj, devices, flows)
      else -> buildTextReport(scopeObj, devices, flows, services, alerts, timeRange)
    }
  }

  fun exportReport(format: String, target: String, timeRange: String): String {
    return generateReport(format, target, timeRange)
  }

  private fun buildTextReport(
    scopeObj: AuthorizedNetworkScope,
    devices: List<ObservedNetworkDevice>,
    flows: List<CommunicationFlow>,
    services: List<ApplicationServiceAnalysis>,
    alerts: List<DefensiveSecurityAlert>,
    timeRange: String
  ): String {
    return buildString {
      appendLine("================================================================================")
      appendLine("REAL NETWORK INTELLIGENCE & AUDIT REPORT - PACKETIVEX")
      appendLine("================================================================================")
      appendLine("Generated At: ${dateFormat.format(Date())}")
      appendLine("Time Range: $timeRange")
      appendLine("Network Name / SSID: ${scopeObj.ssid}")
      appendLine("Subnet: ${scopeObj.subnet} (${scopeObj.cidr})")
      appendLine("Gateway IP: ${scopeObj.gatewayIp}")
      appendLine("DNS Servers: ${scopeObj.dnsServers.joinToString(", ")}")
      appendLine("Monitoring Interface: ${scopeObj.monitoringInterface}")
      appendLine("Monitoring Source: ${scopeObj.monitoringSource}")
      appendLine("Visibility Mode: ${scopeObj.networkVisibilityMode}")
      appendLine("Visibility Details: ${scopeObj.visibilityExplanation}")
      appendLine("--------------------------------------------------------------------------------")
      appendLine("OBSERVED METRICS SUMMARY")
      appendLine("Discovered Devices: ${devices.size} (${devices.count { it.isActive }} online)")
      appendLine("Active Socket Flows: ${flows.size}")
      appendLine("Observed Services: ${services.size}")
      appendLine("Security Findings: ${alerts.size}")
      appendLine("Total Traffic: ${scopeObj.totalTrafficBytes} Bytes (Down: ${scopeObj.totalDownloadBytes} B | Up: ${scopeObj.totalUploadBytes} B)")
      appendLine("--------------------------------------------------------------------------------")
      appendLine("REAL DISCOVERED DEVICES:")
      if (devices.isEmpty()) {
        appendLine("  [No devices observed from this interface / Real network data unavailable]")
      } else {
        devices.forEach { d ->
          appendLine("  • IP: ${d.ipAddress.padEnd(15)} | MAC: ${d.macAddress.padEnd(17)} | Host: ${d.hostname.padEnd(20)} | Vendor: ${d.vendor.padEnd(15)} | Bytes: ${d.totalBytes}")
        }
      }
      appendLine("--------------------------------------------------------------------------------")
      appendLine("REAL ACTIVE COMMUNICATION FLOWS:")
      if (flows.isEmpty()) {
        appendLine("  [No flows observed / Real network data unavailable]")
      } else {
        flows.take(30).forEach { f ->
          appendLine("  • ${f.sourceDeviceIp.padEnd(15)} ➔ ${f.destinationAddress}:${f.port} (${f.protocol}) | Domain: ${f.destinationDomain} | Bytes: ${f.totalBytes}")
        }
      }
      appendLine("--------------------------------------------------------------------------------")
      appendLine("SECURITY FINDINGS & AUDIT NOTES:")
      if (alerts.isEmpty()) {
        appendLine("  [No security anomalies observed on this network interface]")
      } else {
        alerts.forEach { a ->
          appendLine("  • [${a.severity}] ${a.title}: ${a.explanation} | Evidence: ${a.evidence}")
        }
      }
      appendLine("================================================================================")
    }
  }

  private fun buildCsvReport(
    scopeObj: AuthorizedNetworkScope,
    devices: List<ObservedNetworkDevice>,
    flows: List<CommunicationFlow>
  ): String {
    return buildString {
      appendLine("Device_IP,MAC_Address,Hostname,Vendor,Device_Type,Is_Gateway,Is_Local,Total_Bytes,Upload_Bytes,Download_Bytes,Active")
      devices.forEach { d ->
        appendLine("${d.ipAddress},${d.macAddress},\"${d.hostname}\",\"${d.vendor}\",${d.estimatedDeviceType},${d.isGateway},${d.isLocalHost},${d.totalBytes},${d.uploadBytes},${d.downloadBytes},${d.isActive}")
      }
      appendLine("")
      appendLine("Source_IP,Destination_IP,Port,Protocol,Domain,Total_Bytes,Packets,Status")
      flows.forEach { f ->
        appendLine("${f.sourceDeviceIp},${f.destinationAddress},${f.port},${f.protocol},\"${f.destinationDomain}\",${f.totalBytes},${f.packetCount},${f.status}")
      }
    }
  }

  private fun buildJsonReport(
    scopeObj: AuthorizedNetworkScope,
    devices: List<ObservedNetworkDevice>,
    flows: List<CommunicationFlow>,
    services: List<ApplicationServiceAnalysis>,
    alerts: List<DefensiveSecurityAlert>,
    timeRange: String
  ): String {
    return buildString {
      appendLine("{")
      appendLine("  \"report_timestamp\": ${System.currentTimeMillis()},")
      appendLine("  \"time_range\": \"$timeRange\",")
      appendLine("  \"network\": {")
      appendLine("    \"ssid\": \"${scopeObj.ssid}\",")
      appendLine("    \"subnet\": \"${scopeObj.subnet}\",")
      appendLine("    \"gateway\": \"${scopeObj.gatewayIp}\",")
      appendLine("    \"monitoring_interface\": \"${scopeObj.monitoringInterface}\",")
      appendLine("    \"visibility_mode\": \"${scopeObj.networkVisibilityMode}\",")
      appendLine("    \"visibility_explanation\": \"${scopeObj.visibilityExplanation}\"")
      appendLine("  },")
      appendLine("  \"metrics\": {")
      appendLine("    \"device_count\": ${devices.size},")
      appendLine("    \"flow_count\": ${flows.size},")
      appendLine("    \"service_count\": ${services.size},")
      appendLine("    \"alert_count\": ${alerts.size}")
      appendLine("  },")
      appendLine("  \"devices\": [")
      devices.forEachIndexed { i, d ->
        appendLine("    {\"ip\": \"${d.ipAddress}\", \"mac\": \"${d.macAddress}\", \"hostname\": \"${d.hostname}\", \"vendor\": \"${d.vendor}\", \"bytes\": ${d.totalBytes}}${if (i < devices.size - 1) "," else ""}")
      }
      appendLine("  ],")
      appendLine("  \"flows\": [")
      flows.take(25).forEachIndexed { i, f ->
        appendLine("    {\"src\": \"${f.sourceDeviceIp}\", \"dst\": \"${f.destinationAddress}\", \"port\": ${f.port}, \"proto\": \"${f.protocol}\", \"domain\": \"${f.destinationDomain}\", \"bytes\": ${f.totalBytes}}${if (i < minOf(flows.size, 25) - 1) "," else ""}")
      }
      appendLine("  ]")
      appendLine("}")
    }
  }
}
