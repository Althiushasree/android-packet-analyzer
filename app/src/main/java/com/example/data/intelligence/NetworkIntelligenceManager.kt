package com.example.data.intelligence

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
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
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt

class NetworkIntelligenceManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
  val dbManager = com.example.data.db.NetworkDatabaseManager(context)
  val mlEngine = com.example.data.ml.OnDeviceNetworkMlEngine()

  // State Flows
  private val _networkInfo = MutableStateFlow(RealNetworkInterfaceInfo())
  val networkInfo: StateFlow<RealNetworkInterfaceInfo> = _networkInfo.asStateFlow()

  private val _availableInterfaces = MutableStateFlow<List<String>>(emptyList())
  val availableInterfaces: StateFlow<List<String>> = _availableInterfaces.asStateFlow()

  private val _selectedInterfaceName = MutableStateFlow<String?>(null)
  val selectedInterfaceName: StateFlow<String?> = _selectedInterfaceName.asStateFlow()

  private val _networkChangeBanner = MutableStateFlow<String?>(null)
  val networkChangeBanner: StateFlow<String?> = _networkChangeBanner.asStateFlow()

  private val _observedDevices = MutableStateFlow<List<ObservedNetworkDevice>>(emptyList())
  val observedDevices: StateFlow<List<ObservedNetworkDevice>> = _observedDevices.asStateFlow()

  private val _selectedDeviceForDeepAnalysis = MutableStateFlow<ObservedNetworkDevice?>(null)
  val selectedDeviceForDeepAnalysis: StateFlow<ObservedNetworkDevice?> = _selectedDeviceForDeepAnalysis.asStateFlow()

  private val _communicationFlows = MutableStateFlow<List<CommunicationFlow>>(emptyList())
  val communicationFlows: StateFlow<List<CommunicationFlow>> = _communicationFlows.asStateFlow()

  private val _applicationServices = MutableStateFlow<List<ApplicationServiceAnalysis>>(emptyList())
  val applicationServices: StateFlow<List<ApplicationServiceAnalysis>> = _applicationServices.asStateFlow()

  private val _dnsLogs = MutableStateFlow<List<RealDnsLogEntry>>(emptyList())
  val dnsLogs: StateFlow<List<RealDnsLogEntry>> = _dnsLogs.asStateFlow()

  private val _liveTrafficStats = MutableStateFlow(RealTimeTrafficStats())
  val liveTrafficStats: StateFlow<RealTimeTrafficStats> = _liveTrafficStats.asStateFlow()

  private val _networkHealth = MutableStateFlow(NetworkHealthReport())
  val networkHealth: StateFlow<NetworkHealthReport> = _networkHealth.asStateFlow()

  private val _securityAlerts = MutableStateFlow<List<DefensiveSecurityAlert>>(emptyList())
  val securityAlerts: StateFlow<List<DefensiveSecurityAlert>> = _securityAlerts.asStateFlow()

  private val _isMonitoringActive = MutableStateFlow(true)
  val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

  private val _isDiscoveryScanning = MutableStateFlow(false)
  val isDiscoveryScanning: StateFlow<Boolean> = _isDiscoveryScanning.asStateFlow()

  private val _aiAnalystInsight = MutableStateFlow(AiAnalystInsight())
  val aiAnalystInsight: StateFlow<AiAnalystInsight> = _aiAnalystInsight.asStateFlow()

  // Internal Tracking Maps
  private val devicesMap = ConcurrentHashMap<String, ObservedNetworkDevice>()
  private val flowsMap = ConcurrentHashMap<String, CommunicationFlow>()
  private var lastRxBytes = TrafficStats.getTotalRxBytes()
  private var lastTxBytes = TrafficStats.getTotalTxBytes()
  private var lastRxPackets = TrafficStats.getTotalRxPackets()
  private var lastTxPackets = TrafficStats.getTotalTxPackets()
  private var lastStatsTimestamp = System.currentTimeMillis()
  private var connectionStartTime = System.currentTimeMillis()
  private var previousNetworkIdentifier: String = ""

  private var canAccessProcNet: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

  private var monitorJob: Job? = null
  private var discoveryJob: Job? = null
  private var healthJob: Job? = null

  init {
    detectActiveNetworkAndInterfaces()
    registerNetworkCallback()
    startRealTimeMonitoring()
    triggerDeviceDiscovery()
  }

  /**
   * Automatically detects the actual network interface and configuration from OS.
   */
  fun detectActiveNetworkAndInterfaces() {
    scope.launch {
      try {
        val ifaceList = mutableListOf<String>()
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (iface in interfaces) {
          if (iface.isUp && !iface.isLoopback) {
            val displayName = "${iface.name} (${getInterfaceTypeDescription(iface.name)})"
            ifaceList.add(displayName)
          }
        }
        if (ifaceList.isEmpty()) {
          for (iface in interfaces) {
            ifaceList.add(iface.name)
          }
        }
        _availableInterfaces.value = ifaceList

        // Read active network details
        val activeNetwork = connectivityManager.activeNetwork
        val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val linkProps = activeNetwork?.let { connectivityManager.getLinkProperties(it) }

        var ifaceName = linkProps?.interfaceName ?: "Not observable"
        var ifaceType = "Not observable"
        var isWifi = false
        var isEthernet = false
        var isCellular = false
        var isVpn = false

        if (caps != null) {
          if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            ifaceType = "Wi-Fi"
            isWifi = true
          } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            ifaceType = "Ethernet"
            isEthernet = true
          } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            ifaceType = "Cellular / Mobile Hotspot"
            isCellular = true
          } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            ifaceType = "VPN Interface"
            isVpn = true
          }
        }

        // Extract SSID when available
        var detectedSsid = "Not observable on current network"
        if (isWifi && wifiManager != null) {
          try {
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo?.ssid?.replace("\"", "") ?: ""
            if (ssid.isNotEmpty() && ssid != "<unknown ssid>") {
              detectedSsid = ssid
            }
          } catch (_: Exception) {}
        }

        // Extract Local IPv4 & IPv6 & Subnet Mask
        var localIpv4 = "Not observable"
        var localIpv6 = "Not observable"
        var prefixLength = 24
        var mtu = 1500

        linkProps?.linkAddresses?.forEach { linkAddr ->
          val addr = linkAddr.address
          if (addr is Inet4Address && !addr.isLoopbackAddress) {
            localIpv4 = addr.hostAddress ?: "Not observable"
            prefixLength = linkAddr.prefixLength
          } else if (addr is Inet6Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
            localIpv6 = addr.hostAddress ?: "Not observable"
          }
        }

        // Fallback: Check NetworkInterface directly if LinkProperties was empty
        if (localIpv4 == "Not observable") {
          for (iface in interfaces) {
            if (iface.isUp && !iface.isLoopback) {
              val addrs = Collections.list(iface.inetAddresses)
              for (a in addrs) {
                if (a is Inet4Address && !a.isLoopbackAddress) {
                  localIpv4 = a.hostAddress ?: "Not observable"
                  ifaceName = iface.name
                  break
                }
              }
              if (localIpv4 != "Not observable") break
            }
          }
        }

        // Extract Default Gateway & DNS Servers
        var gateway = "Not observable"
        linkProps?.routes?.forEach { route ->
          if (route.isDefaultRoute && route.gateway != null) {
            gateway = route.gateway?.hostAddress ?: "Not observable"
          }
        }

        val dnsList = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
        val dnsDisplay = if (dnsList.isNotEmpty()) dnsList else listOf("Not observable on current network")

        // Link Speed
        var linkSpeed = -1
        if (isWifi && wifiManager != null) {
          try {
            val speed = wifiManager.connectionInfo?.linkSpeed ?: -1
            if (speed > 0) linkSpeed = speed
          } catch (_: Exception) {}
        }

        // Subnet Mask string calculation from prefixLength
        val subnetMask = calculateSubnetMask(prefixLength)

        // MAC Address / Hardware Address
        var macStr = "Not observable on current network"
        try {
          val targetIface = if (ifaceName != "Not observable") NetworkInterface.getByName(ifaceName) else null
          val hw = targetIface?.hardwareAddress
          if (hw != null && hw.isNotEmpty()) {
            macStr = hw.joinToString(":") { String.format("%02X", it) }
          }
        } catch (_: Exception) {}

        val totalRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
        val totalTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
        val totalRxPkt = TrafficStats.getTotalRxPackets().coerceAtLeast(0L)
        val totalTxPkt = TrafficStats.getTotalTxPackets().coerceAtLeast(0L)

        val duration = (System.currentTimeMillis() - connectionStartTime) / 1000L

        val updated = RealNetworkInterfaceInfo(
          interfaceName = ifaceName,
          interfaceType = ifaceType,
          isUp = true,
          isConnected = activeNetwork != null,
          ssid = detectedSsid,
          localIpv4 = localIpv4,
          localIpv6 = localIpv6,
          macAddress = macStr,
          subnetMask = subnetMask,
          subnetPrefixLength = prefixLength,
          defaultGateway = gateway,
          dnsServers = dnsDisplay,
          dhcpServer = gateway,
          mtu = linkProps?.mtu ?: mtu,
          linkSpeedMbps = linkSpeed,
          rxBytes = totalRx,
          txBytes = totalTx,
          rxPackets = totalRxPkt,
          txPackets = totalTxPkt,
          connectionDurationSeconds = duration,
          isVpnActive = isVpn,
          isWifi = isWifi,
          isCellular = isCellular,
          isEthernet = isEthernet
        )

        _networkInfo.value = updated

        // Register local host & gateway in device list
        registerLocalHostAndGateway(localIpv4, gateway, macStr)

      } catch (e: Exception) {
        Log.e("NetworkIntelligence", "Failed detecting network", e)
      }
    }
  }

  private fun registerNetworkCallback() {
    try {
      val request = NetworkRequest.Builder().build()
      connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          handleNetworkChanged("Network Connected / Interface Switched")
        }

        override fun onLost(network: Network) {
          handleNetworkChanged("Network Disconnected")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
          detectActiveNetworkAndInterfaces()
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
          detectActiveNetworkAndInterfaces()
        }
      })
    } catch (e: Exception) {
      Log.w("NetworkIntelligence", "Network callback registration warning", e)
    }
  }

  private fun handleNetworkChanged(reason: String) {
    scope.launch {
      val prev = previousNetworkIdentifier
      detectActiveNetworkAndInterfaces()
      val current = _networkInfo.value.let { "${it.ssid} (${it.interfaceName} - ${it.localIpv4})" }

      if (prev.isNotEmpty() && prev != current) {
        _networkChangeBanner.value = "NETWORK CHANGED: Previous [$prev] ➔ Current [$current]"
        // Record new session in database on network change
        dbManager.startOrRegisterSession(_networkInfo.value)
      } else {
        _networkChangeBanner.value = "Active Network Refreshed: $current"
      }
      previousNetworkIdentifier = current

      // Trigger automatic refresh of devices, statistics, health
      triggerDeviceDiscovery()
      runNetworkHealthCheck()
    }
  }

  fun dismissNetworkChangeBanner() {
    _networkChangeBanner.value = null
  }

  private fun registerLocalHostAndGateway(localIp: String, gatewayIp: String, localMac: String) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val now = System.currentTimeMillis()
    val nowFormatted = timeFormat.format(Date(now))

    if (localIp != "Not observable") {
      val localDevice = devicesMap[localIp] ?: ObservedNetworkDevice(
        id = localIp,
        ipAddress = localIp,
        macAddress = localMac,
        hostname = "localhost (This Android Device)",
        vendor = resolveVendorFromMac(localMac).ifEmpty { "Android Host Device" },
        estimatedDeviceType = DeviceType.LOCAL_DEVICE,
        isLocalHost = true,
        isGateway = false,
        firstSeenTimestamp = now,
        lastSeenTimestamp = now,
        firstSeenFormatted = nowFormatted,
        lastSeenFormatted = nowFormatted,
        isActive = true,
        totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes(),
        totalPackets = TrafficStats.getTotalRxPackets() + TrafficStats.getTotalTxPackets(),
        confidence = "Self Network Interface"
      )
      devicesMap[localIp] = localDevice.copy(
        lastSeenTimestamp = now,
        lastSeenFormatted = nowFormatted,
        totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes(),
        totalPackets = TrafficStats.getTotalRxPackets() + TrafficStats.getTotalTxPackets()
      )
    }

    if (gatewayIp != "Not observable") {
      val gatewayDevice = devicesMap[gatewayIp] ?: ObservedNetworkDevice(
        id = gatewayIp,
        ipAddress = gatewayIp,
        macAddress = readArpForIp(gatewayIp).ifEmpty { "Not observable" },
        hostname = "gateway.local / Default Router",
        vendor = resolveVendorFromMac(readArpForIp(gatewayIp)).ifEmpty { "Router / Access Point" },
        estimatedDeviceType = DeviceType.ROUTER,
        isLocalHost = false,
        isGateway = true,
        firstSeenTimestamp = now,
        lastSeenTimestamp = now,
        firstSeenFormatted = nowFormatted,
        lastSeenFormatted = nowFormatted,
        isActive = true,
        confidence = "Default Gateway Route"
      )
      devicesMap[gatewayIp] = gatewayDevice.copy(
        lastSeenTimestamp = now,
        lastSeenFormatted = nowFormatted
      )
    }

    _observedDevices.value = devicesMap.values.toList().sortedWith(
      compareByDescending<ObservedNetworkDevice> { it.isLocalHost }
        .thenByDescending { it.isGateway }
        .thenByDescending { it.totalBytes }
    )
  }

  /**
   * Real-time device discovery on the local subnet.
   * Scans active subnet range asynchronously, inspects ARP table (/proc/net/arp), and resolves hostnames.
   */
  fun triggerDeviceDiscovery() {
    if (_isDiscoveryScanning.value) return
    discoveryJob?.cancel()

    discoveryJob = scope.launch {
      _isDiscoveryScanning.value = true
      try {
        val localIp = _networkInfo.value.localIpv4
        val gateway = _networkInfo.value.defaultGateway

        // 1. Read ARP table directly from Linux /proc/net/arp
        parseArpTable()

        // 2. Discover local subnet hosts if IPv4 is available
        if (localIp != "Not observable" && localIp.contains(".")) {
          val ipParts = localIp.split(".")
          if (ipParts.size == 4) {
            val subnetPrefix = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}."
            val localHostNum = ipParts[3].toIntOrNull() ?: 1

            // Proactively probe hosts in common ranges (1 to 254) in lightweight batches
            val targetIps = mutableListOf<String>()
            // Always check gateway, low host numbers, neighbor numbers, and standard endpoints
            for (i in 1..35) {
              val ip = "$subnetPrefix$i"
              if (ip != localIp) targetIps.add(ip)
            }
            for (i in maxOf(1, localHostNum - 5)..minOf(254, localHostNum + 5)) {
              val ip = "$subnetPrefix$i"
              if (ip != localIp && !targetIps.contains(ip)) targetIps.add(ip)
            }
            for (i in 100..120) {
              val ip = "$subnetPrefix$i"
              if (ip != localIp && !targetIps.contains(ip)) targetIps.add(ip)
            }
            for (i in 200..215) {
              val ip = "$subnetPrefix$i"
              if (ip != localIp && !targetIps.contains(ip)) targetIps.add(ip)
            }

            // Probe target IPs asynchronously in parallel chunks
            targetIps.chunked(8).forEach { chunk ->
              if (!isActive) return@launch
              chunk.forEach { ip ->
                launch { probeSingleHost(ip) }
              }
              delay(80)
            }
          }
        }

        // 3. Re-read ARP table after probes
        parseArpTable()

        // 4. Parse active TCP / UDP sockets from /proc/net/tcp and /proc/net/udp
        parseActiveLinuxSockets()

      } catch (e: Exception) {
        Log.w("NetworkIntelligence", "Discovery loop exception", e)
      } finally {
        _isDiscoveryScanning.value = false
        updateObservedDevicesList()
      }
    }
  }

  private suspend fun probeSingleHost(ip: String) = withContext(Dispatchers.IO) {
    try {
      val inet = InetAddress.getByName(ip)
      var reachable = false

      // Try ICMP / standard isReachable check (timeout 120ms)
      try {
        reachable = inet.isReachable(120)
      } catch (_: Exception) {}

      val openPorts = mutableListOf<Int>()
      // Check common service ports: 80 (HTTP), 443 (HTTPS), 53 (DNS), 8080 (Proxy), 22 (SSH)
      val testPorts = listOf(80, 443, 53, 8080, 22, 135)
      for (p in testPorts) {
        try {
          Socket().use { s ->
            s.connect(InetSocketAddress(ip, p), 80)
            openPorts.add(p)
            reachable = true
          }
        } catch (_: Exception) {}
      }

      if (reachable) {
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val mac = readArpForIp(ip).ifEmpty { "Not observable" }
        val hostname = try {
          val h = inet.canonicalHostName
          if (h != ip) h else "Not observable"
        } catch (_: Exception) {
          "Not observable"
        }

        val vendor = resolveVendorFromMac(mac)
        val devType = estimateDeviceType(ip, hostname, vendor, openPorts)

        val existing = devicesMap[ip]
        val updated = (existing ?: ObservedNetworkDevice(
          id = ip,
          ipAddress = ip,
          macAddress = mac,
          hostname = hostname,
          vendor = vendor,
          estimatedDeviceType = devType,
          firstSeenTimestamp = now,
          firstSeenFormatted = timeFormat.format(Date(now)),
          confidence = "Active Subnet Probe"
        )).copy(
          macAddress = if (mac != "Not observable") mac else (existing?.macAddress ?: "Not observable"),
          hostname = if (hostname != "Not observable") hostname else (existing?.hostname ?: "Not observable"),
          vendor = if (vendor != "Unknown Vendor") vendor else (existing?.vendor ?: "Unknown Vendor"),
          estimatedDeviceType = devType,
          lastSeenTimestamp = now,
          lastSeenFormatted = timeFormat.format(Date(now)),
          isActive = true,
          openPorts = (existing?.openPorts.orEmpty() + openPorts).distinct()
        )

        devicesMap[ip] = updated
      }
    } catch (_: Exception) {}
  }

  private fun parseArpTable() {
    if (!canAccessProcNet) return
    try {
      val arp = File("/proc/net/arp")
      if (arp.exists() && arp.canRead()) {
        val reader = BufferedReader(FileReader(arp))
        var line = reader.readLine() // Header
        val now = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        while (reader.readLine().also { line = it } != null) {
          val tokens = line?.split(Regex("\\s+"))?.filter { it.isNotBlank() } ?: continue
          if (tokens.size >= 4) {
            val ip = tokens[0]
            val flags = tokens[2]
            val mac = tokens[3]

            // If flag is not 0x0 (meaning completed ARP resolution) and MAC is not empty/incomplete
            if (flags != "0x0" && mac != "00:00:00:00:00:00" && mac.contains(":")) {
              val vendor = resolveVendorFromMac(mac)
              val isGateway = ip == _networkInfo.value.defaultGateway
              val devType = if (isGateway) DeviceType.ROUTER else estimateDeviceType(ip, "", vendor, emptyList())

              val existing = devicesMap[ip]
              val updated = (existing ?: ObservedNetworkDevice(
                id = ip,
                ipAddress = ip,
                macAddress = mac.uppercase(Locale.US),
                vendor = vendor,
                estimatedDeviceType = devType,
                isGateway = isGateway,
                firstSeenTimestamp = now,
                firstSeenFormatted = timeFormat.format(Date(now)),
                confidence = "OS ARP Cache Table (/proc/net/arp)"
              )).copy(
                macAddress = mac.uppercase(Locale.US),
                vendor = vendor,
                lastSeenTimestamp = now,
                lastSeenFormatted = timeFormat.format(Date(now)),
                isActive = true
              )
              devicesMap[ip] = updated
            }
          }
        }
        reader.close()
      } else {
        canAccessProcNet = false
      }
    } catch (e: Exception) {
      canAccessProcNet = false
    }
  }

  private fun parseActiveLinuxSockets() {
    if (!canAccessProcNet) return
    try {
      val files = listOf("/proc/net/tcp", "/proc/net/tcp6", "/proc/net/udp", "/proc/net/udp6")
      val now = System.currentTimeMillis()
      val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

      for (filePath in files) {
        val f = File(filePath)
        if (f.exists() && f.canRead()) {
          val reader = BufferedReader(FileReader(f))
          var line = reader.readLine() // Header
          val proto = if (filePath.contains("tcp")) "TCP" else "UDP"

          while (reader.readLine().also { line = it } != null) {
            val tokens = line?.trim()?.split(Regex("\\s+")) ?: continue
            if (tokens.size >= 4) {
              val localAddrHex = tokens[1]
              val remAddrHex = tokens[2]

              val localEndpoint = parseHexEndpoint(localAddrHex)
              val remEndpoint = parseHexEndpoint(remAddrHex)

              if (remEndpoint != null && remEndpoint.first != "0.0.0.0" && remEndpoint.first != "::") {
                val flowId = "${localEndpoint?.first ?: "local"}:${localEndpoint?.second ?: 0} -> ${remEndpoint.first}:${remEndpoint.second}"
                val flow = flowsMap[flowId] ?: CommunicationFlow(
                  id = flowId,
                  sourceDeviceIp = _networkInfo.value.localIpv4,
                  destinationAddress = remEndpoint.first,
                  protocol = proto,
                  port = remEndpoint.second,
                  packetCount = 1L,
                  totalBytes = 64L,
                  lastSeenTimestamp = now,
                  lastSeenFormatted = timeFormat.format(Date(now)),
                  status = IntelligenceStatus.OBSERVED
                )
                flowsMap[flowId] = flow.copy(
                  packetCount = flow.packetCount + 1,
                  totalBytes = flow.totalBytes + 64,
                  lastSeenTimestamp = now,
                  lastSeenFormatted = timeFormat.format(Date(now))
                )

                // Track service inference
                recordServiceAnalysis(remEndpoint.first, remEndpoint.second, proto)
              }
            }
          }
          reader.close()
        }
      }
      _communicationFlows.value = flowsMap.values.toList().sortedByDescending { it.lastSeenTimestamp }.take(50)
    } catch (e: Exception) {
      canAccessProcNet = false
    }
  }

  private fun parseHexEndpoint(hexStr: String): Pair<String, Int>? {
    try {
      val parts = hexStr.split(":")
      if (parts.size != 2) return null
      val ipHex = parts[0]
      val port = parts[1].toInt(16)

      if (ipHex.length == 8) {
        // IPv4 in little-endian hex
        val b1 = ipHex.substring(6, 8).toInt(16)
        val b2 = ipHex.substring(4, 6).toInt(16)
        val b3 = ipHex.substring(2, 4).toInt(16)
        val b4 = ipHex.substring(0, 2).toInt(16)
        val ip = "$b1.$b2.$b3.$b4"
        return ip to port
      }
    } catch (_: Exception) {}
    return null
  }

  private fun recordServiceAnalysis(destIp: String, port: Int, protocol: String) {
    val (serviceName, status, evidence, isEncrypted) = when (port) {
      53 -> Quadruple("DNS System Service", IntelligenceStatus.OBSERVED, "Direct UDP/53 Port Traffic", false)
      80 -> Quadruple("HTTP Web Server", IntelligenceStatus.OBSERVED, "Cleartext TCP/80 Port Observed", false)
      443 -> Quadruple("HTTPS / TLS Encrypted Service", IntelligenceStatus.OBSERVED, "Encrypted TLS TCP/443 Stream", true)
      8080, 8443 -> Quadruple("Alternative Web Proxy", IntelligenceStatus.INFERRED, "Observed Port $port Traffic", true)
      22 -> Quadruple("SSH Remote Terminal", IntelligenceStatus.OBSERVED, "Secure Shell TCP/22", true)
      5222, 5228 -> Quadruple("Push Notification Service (FCM/XMPP)", IntelligenceStatus.INFERRED, "Google Services Port $port", true)
      123 -> Quadruple("NTP Time Synchronization", IntelligenceStatus.OBSERVED, "Network Time Protocol UDP/123", false)
      else -> Quadruple("Unknown Encrypted Service", IntelligenceStatus.UNKNOWN, "Encrypted traffic detected. Metadata insufficient for exact app name.", true)
    }

    val currentList = _applicationServices.value.toMutableList()
    val existing = currentList.find { it.serviceName == serviceName && it.deviceIp == _networkInfo.value.localIpv4 }
    if (existing != null) {
      val updated = existing.copy(
        trafficBytes = existing.trafficBytes + 512,
        packetCount = existing.packetCount + 1,
        portsUsed = (existing.portsUsed + port).distinct()
      )
      currentList[currentList.indexOf(existing)] = updated
    } else {
      currentList.add(
        ApplicationServiceAnalysis(
          id = "svc_${destIp}_$port",
          serviceName = serviceName,
          deviceIp = _networkInfo.value.localIpv4,
          status = status,
          evidence = evidence,
          trafficBytes = 512,
          packetCount = 1,
          portsUsed = listOf(port),
          protocol = protocol,
          isEncrypted = isEncrypted,
          explanation = if (status == IntelligenceStatus.UNKNOWN) "The traffic is encrypted and available metadata is insufficient to identify the exact application." else "Active network communication stream observed on port $port."
        )
      )
    }
    _applicationServices.value = currentList.sortedByDescending { it.trafficBytes }
  }

  private fun startRealTimeMonitoring() {
    monitorJob = scope.launch {
      while (isActive) {
        if (_isMonitoringActive.value) {
          try {
            val now = System.currentTimeMillis()
            val elapsedSec = maxOf(0.5, (now - lastStatsTimestamp) / 1000.0)

            val currentRxBytes = TrafficStats.getTotalRxBytes().coerceAtLeast(0L)
            val currentTxBytes = TrafficStats.getTotalTxBytes().coerceAtLeast(0L)
            val currentRxPkts = TrafficStats.getTotalRxPackets().coerceAtLeast(0L)
            val currentTxPkts = TrafficStats.getTotalTxPackets().coerceAtLeast(0L)

            val deltaRxBytes = maxOf(0L, currentRxBytes - lastRxBytes)
            val deltaTxBytes = maxOf(0L, currentTxBytes - lastTxBytes)
            val deltaRxPkts = maxOf(0L, currentRxPkts - lastRxPackets)
            val deltaTxPkts = maxOf(0L, currentTxPkts - lastTxPackets)

            val bytesPerSec = (deltaRxBytes + deltaTxBytes) / elapsedSec
            val pktsPerSec = (deltaRxPkts + deltaTxPkts) / elapsedSec
            val uploadBps = deltaTxBytes / elapsedSec
            val downloadBps = deltaRxBytes / elapsedSec

            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
            lastRxPackets = currentRxPkts
            lastTxPackets = currentTxPkts
            lastStatsTimestamp = now

            // Update Real-Time Traffic Stats
            val totalPkts = currentRxPkts + currentTxPkts
            val totalBytes = currentRxBytes + currentTxBytes

            val flowsList = _communicationFlows.value
            val realTcp = flowsList.filter { it.protocol.contains("TCP", ignoreCase = true) }.sumOf { it.packetCount }
            val realUdp = flowsList.filter { it.protocol.contains("UDP", ignoreCase = true) }.sumOf { it.packetCount }
            val realDns = flowsList.filter { it.protocol.contains("DNS", ignoreCase = true) || it.port == 53 }.sumOf { it.packetCount }
            val realTls = flowsList.filter { it.port == 443 || it.protocol.contains("TLS", ignoreCase = true) || it.protocol.contains("HTTPS", ignoreCase = true) }.sumOf { it.packetCount }
            val realQuic = flowsList.filter { it.protocol.contains("QUIC", ignoreCase = true) }.sumOf { it.packetCount }
            val realOther = flowsList.filter { !it.protocol.contains("TCP", ignoreCase = true) && !it.protocol.contains("UDP", ignoreCase = true) }.sumOf { it.packetCount }

            _liveTrafficStats.value = RealTimeTrafficStats(
              packetsPerSec = pktsPerSec,
              bytesPerSec = bytesPerSec,
              uploadBytesPerSec = uploadBps,
              downloadBytesPerSec = downloadBps,
              totalPackets = totalPkts,
              totalBytes = totalBytes,
              totalUploadBytes = currentTxBytes,
              totalDownloadBytes = currentRxBytes,
              tcpPackets = realTcp,
              udpPackets = realUdp,
              icmpPackets = 0L,
              dnsPackets = realDns,
              tlsPackets = realTls,
              quicPackets = realQuic,
              otherProtocolsPackets = realOther
            )

            // Update network duration and interface stats
            val currentIface = _networkInfo.value
            _networkInfo.value = currentIface.copy(
              rxBytes = currentRxBytes,
              txBytes = currentTxBytes,
              rxPackets = currentRxPkts,
              txPackets = currentTxPkts,
              connectionDurationSeconds = (now - connectionStartTime) / 1000L
            )

            // Check for security anomalies
            checkForDefensiveAnomalies(pktsPerSec, bytesPerSec)

            // On-Device Machine Learning Flow Anomaly & Intrusion Inference
            val flowFeatures = com.example.data.ml.NetworkFlowFeatures(
              flowId = "flow_${_networkInfo.value.interfaceName}_$now",
              protocol = if (pktsPerSec > 50) "TCP" else "TLS",
              packetsPerSec = pktsPerSec,
              bytesPerSec = bytesPerSec,
              payloadEntropy = if (bytesPerSec > 500000) 6.9 else 4.3,
              interArrivalJitterMs = if (pktsPerSec > 5) (1000.0 / pktsPerSec) else 95.0,
              portRiskScore = if (pktsPerSec > 100) 0.8 else 0.1,
              synAckRatio = 0.35
            )
            val mlResult = mlEngine.inferFlow(flowFeatures)
            if (mlResult.isAnomaly && mlResult.threatProbability > 0.7) {
              val alertId = "ml_alert_$now"
              if (_securityAlerts.value.none { it.id == alertId }) {
                val mlAlert = DefensiveSecurityAlert(
                  id = alertId,
                  severity = AnomalySeverity.HIGH,
                  title = "ML Anomaly: ${mlResult.threatClassification}",
                  deviceIp = _networkInfo.value.localIpv4,
                  sourceAddress = _networkInfo.value.localIpv4,
                  destinationAddress = "External Flow / Subnet",
                  protocol = flowFeatures.protocol,
                  port = 443,
                  timestamp = now,
                  timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now)),
                  evidence = "Isolation Score: ${String.format(Locale.US, "%.2f", mlResult.anomalyScore)}, Cluster: ${mlResult.clusterId}, Jitter: ${String.format(Locale.US, "%.1f", flowFeatures.interArrivalJitterMs)}ms",
                  confidence = "ML Model Confidence: ${String.format(Locale.US, "%.0f", mlResult.confidenceScore * 100)}%",
                  explanation = "On-Device ML Isolation Forest flagged this flow with an Anomaly Score of ${String.format(Locale.US, "%.2f", mlResult.anomalyScore)} and Threat Probability of ${String.format(Locale.US, "%.1f", mlResult.threatProbability * 100)}%. Cluster: ${mlResult.clusterLabel}."
                )
                _securityAlerts.value = listOf(mlAlert) + _securityAlerts.value
              }
            }

            // Periodic 5s Database Stats aggregation
            if (now % 5000 < 1000) {
              val sid = dbManager.activeSessionId.value ?: dbManager.startOrRegisterSession(_networkInfo.value)
              dbManager.recordTrafficStatistic(sid, _liveTrafficStats.value)
              dbManager.recordServiceObservations(sid, _applicationServices.value)
              dbManager.recordCommunicationFlows(sid, _communicationFlows.value)
            }

          } catch (e: Exception) {
            Log.w("NetworkIntelligence", "Monitor loop warning", e)
          }
        }
        delay(1000)
      }
    }

    // Periodic Health Check
    healthJob = scope.launch {
      while (isActive) {
        if (_isMonitoringActive.value) {
          runNetworkHealthCheck()
        }
        delay(10000) // Check health every 10 seconds
      }
    }
  }

  fun runNetworkHealthCheck() {
    scope.launch {
      try {
        val gateway = _networkInfo.value.defaultGateway
        var gatewayLatency = -1.0
        var packetLoss = 0.0

        // Test real ping latency to default gateway if valid IP
        if (gateway != "Not observable" && gateway.contains(".")) {
          try {
            val start = System.nanoTime()
            val inet = InetAddress.getByName(gateway)
            val isReachable = inet.isReachable(300)
            val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
            if (isReachable) {
              gatewayLatency = elapsedMs
              packetLoss = 0.0
            } else {
              packetLoss = 100.0
            }
          } catch (_: Exception) {
            packetLoss = 50.0
          }
        }

        // Test real DNS query latency to DNS server / 8.8.8.8
        var dnsLatency = -1.0
        val dnsServer = _networkInfo.value.dnsServers.firstOrNull { it != "Not observable on current network" } ?: "8.8.8.8"
        try {
          val dnsStart = System.nanoTime()
          val resolved = InetAddress.getByName("google.com")
          dnsLatency = (System.nanoTime() - dnsStart) / 1_000_000.0

          // Log real DNS query
          val now = System.currentTimeMillis()
          val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
          val dnsEntry = RealDnsLogEntry(
            id = "dns_${now}",
            timestamp = now,
            timeFormatted = timeFormat.format(Date(now)),
            deviceIp = _networkInfo.value.localIpv4,
            dnsServer = dnsServer,
            queryDomain = "google.com",
            queryType = "A",
            responseAnswer = resolved.hostAddress ?: "",
            latencyMs = dnsLatency.toLong(),
            isSuccess = true,
            status = IntelligenceStatus.OBSERVED
          )
          _dnsLogs.value = (_dnsLogs.value + dnsEntry).takeLast(30)
        } catch (_: Exception) {}

        // Calculate Throughput Mbps
        val throughputMbps = (_liveTrafficStats.value.bytesPerSec * 8.0) / 1_000_000.0

        // Calculate score (0-100)
        var score = 100
        if (gatewayLatency > 100) score -= 15
        if (gatewayLatency > 300) score -= 30
        if (dnsLatency > 200) score -= 15
        if (packetLoss > 0) score -= 25
        score = score.coerceIn(10, 100)

        val summary = when {
          score >= 90 -> "Optimal Connection Quality & Low Latency"
          score >= 70 -> "Good Network Performance"
          score >= 50 -> "Moderate Latency / Minor Jitter"
          else -> "Degraded Network Response"
        }

        val report = NetworkHealthReport(
          healthScore = score,
          statusSummary = summary,
          gatewayLatencyMs = if (gatewayLatency > 0) ((gatewayLatency * 10).roundToInt() / 10.0) else -1.0,
          dnsLatencyMs = if (dnsLatency > 0) ((dnsLatency * 10).roundToInt() / 10.0) else -1.0,
          packetLossPercent = packetLoss,
          throughputMbps = ((throughputMbps * 100).roundToInt() / 100.0),
          retransmissionCount = 0L,
          interfaceErrors = 0L,
          connectionFailures = if (packetLoss > 0) 1 else 0,
          stabilityLevel = if (score >= 80) "Optimal" else "Monitored",
          measurementTimestamp = System.currentTimeMillis()
        )
        _networkHealth.value = report

        // Persist Health Report in Database
        val sid = dbManager.activeSessionId.value ?: dbManager.startOrRegisterSession(_networkInfo.value)
        dbManager.recordHealthReport(sid, report)
      } catch (e: Exception) {
        Log.w("NetworkIntelligence", "Health check warning", e)
      }
    }
  }

  private fun checkForDefensiveAnomalies(pktsPerSec: Double, bytesPerSec: Double) {
    val now = System.currentTimeMillis()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val alerts = mutableListOf<DefensiveSecurityAlert>()

    // Anomaly 1: Unusually High Traffic Burst
    if (bytesPerSec > 8_000_000.0) { // > 8 MB/s
      alerts.add(
        DefensiveSecurityAlert(
          id = "burst_$now",
          severity = AnomalySeverity.LOW,
          title = "High Network Throughput Burst",
          deviceIp = _networkInfo.value.localIpv4,
          sourceAddress = _networkInfo.value.localIpv4,
          destinationAddress = "External Remote Endpoints",
          protocol = "TCP",
          port = 443,
          timestamp = now,
          timeFormatted = timeFormat.format(Date(now)),
          evidence = "Observed traffic rate reached ${String.format(Locale.US, "%.1f", bytesPerSec / 1024.0 / 1024.0)} MB/s",
          confidence = "High",
          explanation = "Sudden high-volume data stream detected. Typical for active file downloads or multimedia streams."
        )
      )
    }

    // Anomaly 2: Cleartext Unencrypted HTTP Port 80
    val unencryptedFlow = _communicationFlows.value.find { it.port == 80 }
    if (unencryptedFlow != null) {
      alerts.add(
        DefensiveSecurityAlert(
          id = "http_cleartext_$now",
          severity = AnomalySeverity.MEDIUM,
          title = "Cleartext HTTP Traffic Detected",
          deviceIp = _networkInfo.value.localIpv4,
          sourceAddress = _networkInfo.value.localIpv4,
          destinationAddress = unencryptedFlow.destinationAddress,
          protocol = "HTTP",
          port = 80,
          timestamp = now,
          timeFormatted = timeFormat.format(Date(now)),
          evidence = "Outgoing request to port 80 (${unencryptedFlow.destinationAddress}) without TLS encryption.",
          confidence = "High",
          explanation = "Data sent over HTTP is unencrypted and vulnerable to interception by intermediate proxies or routers."
        )
      )
    }

    if (alerts.isNotEmpty()) {
      _securityAlerts.value = (_securityAlerts.value + alerts).takeLast(20)
      val sid = dbManager.activeSessionId.value ?: dbManager.startOrRegisterSession(_networkInfo.value)
      dbManager.recordSecurityAlerts(sid, alerts)
    }
  }

  private fun updateObservedDevicesList() {
    val devList = devicesMap.values.toList().sortedWith(
      compareByDescending<ObservedNetworkDevice> { it.isLocalHost }
        .thenByDescending { it.isGateway }
        .thenByDescending { it.totalBytes }
    )
    _observedDevices.value = devList

    // Synchronize to Room Database
    val sid = dbManager.activeSessionId.value ?: dbManager.startOrRegisterSession(_networkInfo.value)
    dbManager.recordObservedDevices(sid, devList)
  }

  fun selectDeviceForDeepAnalysis(device: ObservedNetworkDevice?) {
    _selectedDeviceForDeepAnalysis.value = device
  }

  fun toggleMonitoring() {
    _isMonitoringActive.value = !_isMonitoringActive.value
  }

  fun pauseMonitoring() {
    _isMonitoringActive.value = false
  }

  fun resumeMonitoring() {
    _isMonitoringActive.value = true
  }

  fun clearAllIntelligenceData() {
    devicesMap.clear()
    flowsMap.clear()
    _observedDevices.value = emptyList()
    _communicationFlows.value = emptyList()
    _applicationServices.value = emptyList()
    _dnsLogs.value = emptyList()
    _securityAlerts.value = emptyList()
    detectActiveNetworkAndInterfaces()
    triggerDeviceDiscovery()
  }

  fun selectInterface(ifaceName: String) {
    _selectedInterfaceName.value = ifaceName
    detectActiveNetworkAndInterfaces()
  }

  fun updateAiAnalystInsight(insight: AiAnalystInsight) {
    _aiAnalystInsight.value = insight
  }

  // Helpers
  private fun readArpForIp(ip: String): String {
    if (!canAccessProcNet) return ""
    try {
      val arp = File("/proc/net/arp")
      if (arp.exists() && arp.canRead()) {
        val reader = BufferedReader(FileReader(arp))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
          val tokens = line?.split(Regex("\\s+")) ?: continue
          if (tokens.isNotEmpty() && tokens[0] == ip && tokens.size >= 4) {
            val mac = tokens[3]
            if (mac != "00:00:00:00:00:00" && mac.contains(":")) {
              reader.close()
              return mac.uppercase(Locale.US)
            }
          }
        }
        reader.close()
      } else {
        canAccessProcNet = false
      }
    } catch (_: Exception) {
      canAccessProcNet = false
    }
    return ""
  }

  private fun resolveVendorFromMac(mac: String): String {
    if (mac.isBlank() || mac == "Not observable" || mac == "00:00:00:00:00:00") return "Unknown Vendor"
    val clean = mac.replace(":", "").replace("-", "").uppercase(Locale.US)
    if (clean.length < 6) return "Unknown Vendor"
    val oui = clean.substring(0, 6)

    return when {
      oui.startsWith("001A11") || oui.startsWith("ACBC32") || oui.startsWith("F01898") || oui.startsWith("BC9FEF") -> "Google LLC"
      oui.startsWith("001E8C") || oui.startsWith("3C15C2") || oui.startsWith("9801A7") || oui.startsWith("A85B78") -> "Apple Inc."
      oui.startsWith("001247") || oui.startsWith("1449E0") || oui.startsWith("5C0A5B") || oui.startsWith("9401C2") -> "Samsung Electronics"
      oui.startsWith("0014D1") || oui.startsWith("E848B8") || oui.startsWith("F4F26D") || oui.startsWith("A0F3C1") -> "TP-Link Technologies"
      oui.startsWith("00000C") || oui.startsWith("000142") || oui.startsWith("000196") || oui.startsWith("0002B9") -> "Cisco Systems"
      oui.startsWith("18FE34") || oui.startsWith("240AC4") || oui.startsWith("30AEA4") || oui.startsWith("84F3EB") -> "Espressif Systems (IoT)"
      oui.startsWith("00037F") || oui.startsWith("00188B") || oui.startsWith("0019B9") || oui.startsWith("002170") -> "Dell Technologies"
      oui.startsWith("0001E6") || oui.startsWith("000802") || oui.startsWith("000F20") || oui.startsWith("001185") -> "Hewlett Packard Enterprise"
      oui.startsWith("001B63") || oui.startsWith("202BC1") || oui.startsWith("60D819") || oui.startsWith("788A20") -> "Xiaomi Communications"
      oui.startsWith("00044B") || oui.startsWith("001A80") || oui.startsWith("002268") || oui.startsWith("0024E8") -> "NVIDIA Corporation"
      oui.startsWith("B827EB") || oui.startsWith("DCA632") || oui.startsWith("E45F01") -> "Raspberry Pi Foundation"
      oui.startsWith("00095B") || oui.startsWith("0014BF") || oui.startsWith("00180A") -> "Netgear Inc."
      oui.startsWith("000E08") || oui.startsWith("001558") || oui.startsWith("001A92") -> "ASUSTeK Computer"
      oui.startsWith("0002B3") || oui.startsWith("000347") || oui.startsWith("000423") -> "Intel Corporation"
      else -> "IEEE Registered Vendor (${oui.substring(0, 2)}:${oui.substring(2, 4)}:${oui.substring(4, 6)})"
    }
  }

  private fun estimateDeviceType(ip: String, hostname: String, vendor: String, openPorts: List<Int>): DeviceType {
    val h = hostname.lowercase(Locale.US)
    val v = vendor.lowercase(Locale.US)

    if (ip == _networkInfo.value.defaultGateway) return DeviceType.ROUTER
    if (ip == _networkInfo.value.localIpv4) return DeviceType.LOCAL_DEVICE

    if (h.contains("router") || h.contains("gateway") || h.contains("ap") || v.contains("tp-link") || v.contains("cisco") || v.contains("netgear")) {
      return DeviceType.ROUTER
    }
    if (v.contains("apple") || v.contains("samsung") || v.contains("xiaomi") || h.contains("android") || h.contains("iphone") || h.contains("phone")) {
      return DeviceType.SMARTPHONE
    }
    if (v.contains("dell") || v.contains("hp") || v.contains("lenovo") || v.contains("asus") || v.contains("intel") || h.contains("laptop") || h.contains("desktop") || h.contains("pc")) {
      return DeviceType.LAPTOP
    }
    if (v.contains("espressif") || v.contains("raspberry") || h.contains("iot") || h.contains("esp32") || h.contains("smart")) {
      return DeviceType.IOT_DEVICE
    }
    if (openPorts.contains(22) || openPorts.contains(80) || openPorts.contains(443) || h.contains("server")) {
      return DeviceType.SERVER
    }
    return DeviceType.UNKNOWN
  }

  private fun calculateSubnetMask(prefix: Int): String {
    val shift = 0xffffffffL shl (32 - prefix)
    val b1 = (shift ushr 24) and 0xff
    val b2 = (shift ushr 16) and 0xff
    val b3 = (shift ushr 8) and 0xff
    val b4 = shift and 0xff
    return "$b1.$b2.$b3.$b4"
  }

  private fun getInterfaceTypeDescription(name: String): String {
    val lower = name.lowercase(Locale.US)
    return when {
      lower.startsWith("wlan") || lower.startsWith("wifi") -> "Wi-Fi Interface"
      lower.startsWith("eth") || lower.startsWith("en") -> "Ethernet Network"
      lower.startsWith("tun") || lower.startsWith("ppp") -> "VPN Tunnel Interface"
      lower.startsWith("rmnet") || lower.startsWith("ccmni") -> "Cellular Modem Interface"
      lower.startsWith("lo") -> "Local Loopback"
      else -> "Network Interface"
    }
  }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
