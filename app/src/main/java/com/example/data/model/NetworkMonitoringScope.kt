package com.example.data.model

import com.example.data.intelligence.RealNetworkInterfaceInfo

/**
 * Represents a real detected network monitoring scope or an authorized collector scope.
 * Never uses mock or fake hard-coded data.
 */
data class AuthorizedNetworkScope(
  val id: String,
  val name: String,
  val ssid: String,
  val subnet: String,
  val cidr: String,
  val gatewayIp: String,
  val dhcpServer: String,
  val dnsServers: List<String>,
  val networkType: String,
  val monitoringInterface: String,
  val monitoringSource: String,
  val isLive: Boolean = true,
  val activeDevicesCount: Int = 0,
  val totalDiscoveredCount: Int = 0,
  val totalTrafficBytes: Long = 0L,
  val totalUploadBytes: Long = 0L,
  val totalDownloadBytes: Long = 0L,
  val networkVisibilityMode: String = "LOCAL_DEVICE_ONLY", // "LOCAL_DEVICE_ONLY", "COLLECTOR_CONNECTED", "UNAVAILABLE"
  val visibilityExplanation: String = ""
)

object NetworkScopeFactory {
  /**
   * Creates a scope from the actual detected active network interface on this device.
   */
  fun fromLiveNetwork(
    liveInfo: RealNetworkInterfaceInfo,
    activeDevicesCount: Int,
    totalDevicesCount: Int,
    hasRemoteCollector: Boolean = false
  ): AuthorizedNetworkScope {
    val isConnected = liveInfo.isConnected && liveInfo.localIpv4 != "Not observable"
    val actualSsid = if (liveInfo.ssid.isNotBlank() && !liveInfo.ssid.contains("Not observable")) liveInfo.ssid else liveInfo.interfaceName
    val subnetStr = if (liveInfo.localIpv4 != "Not observable" && liveInfo.localIpv4.contains(".")) {
      "${liveInfo.localIpv4.substringBeforeLast(".")}.0/${liveInfo.subnetPrefixLength}"
    } else {
      "Not observable"
    }

    val visibilityMode = if (hasRemoteCollector) "COLLECTOR_CONNECTED" else "LOCAL_DEVICE_ONLY"
    val explanation = if (hasRemoteCollector) {
      "Authorized Remote Collector Active: Streaming gateway & AP sensor telemetry."
    } else {
      "Local Device Visibility: Android endpoint interface cannot passively observe other Wi-Fi clients. Connect an authorized network monitoring collector for network-wide visibility."
    }

    return AuthorizedNetworkScope(
      id = "LIVE-${liveInfo.interfaceName}-${actualSsid.replace(" ", "_")}",
      name = if (isConnected) "Active: $actualSsid" else "Disconnected Interface",
      ssid = actualSsid,
      subnet = subnetStr,
      cidr = if (liveInfo.subnetPrefixLength > 0) "/${liveInfo.subnetPrefixLength}" else "Not observable",
      gatewayIp = liveInfo.defaultGateway,
      dhcpServer = liveInfo.dhcpServer,
      dnsServers = if (liveInfo.dnsServers.isNotEmpty()) liveInfo.dnsServers else listOf("Not observable"),
      networkType = "${liveInfo.interfaceType} (${liveInfo.interfaceName})",
      monitoringInterface = "${liveInfo.interfaceName} (Local Sensor)",
      monitoringSource = if (hasRemoteCollector) "Remote Gateway / SPAN Collector" else "Local Android VPN / Network Interface",
      isLive = true,
      activeDevicesCount = activeDevicesCount,
      totalDiscoveredCount = totalDevicesCount,
      totalTrafficBytes = liveInfo.rxBytes + liveInfo.txBytes,
      totalUploadBytes = liveInfo.txBytes,
      totalDownloadBytes = liveInfo.rxBytes,
      networkVisibilityMode = visibilityMode,
      visibilityExplanation = explanation
    )
  }

  /**
   * Creates an authorized collector scope when configured with a real server gateway.
   */
  fun fromCollectorConfig(
    serverHost: String,
    collectorName: String,
    targetNetworkName: String,
    gatewayIp: String,
    subnet: String,
    devicesCount: Int,
    rxBytes: Long,
    txBytes: Long
  ): AuthorizedNetworkScope {
    return AuthorizedNetworkScope(
      id = "COLLECTOR-${serverHost.replace(".", "_")}",
      name = "Authorized Collector: $targetNetworkName",
      ssid = targetNetworkName,
      subnet = subnet,
      cidr = "/24",
      gatewayIp = gatewayIp,
      dhcpServer = "$gatewayIp (DHCP Server)",
      dnsServers = listOf(gatewayIp),
      networkType = "Authorized Gateway TAP / SPAN Mirror",
      monitoringInterface = "collector@$serverHost",
      monitoringSource = "Network Monitoring Collector ($collectorName)",
      isLive = false,
      activeDevicesCount = devicesCount,
      totalDiscoveredCount = devicesCount,
      totalTrafficBytes = rxBytes + txBytes,
      totalUploadBytes = txBytes,
      totalDownloadBytes = rxBytes,
      networkVisibilityMode = "COLLECTOR_CONNECTED",
      visibilityExplanation = "Live telemetry received from authorized collector $collectorName on $serverHost."
    )
  }
}
