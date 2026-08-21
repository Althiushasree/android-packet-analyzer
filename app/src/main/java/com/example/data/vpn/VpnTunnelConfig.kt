package com.example.data.vpn

import java.io.Serializable

/**
 * Detailed configuration parameters for establishing the VpnService TUN virtual interface.
 */
data class VpnTunnelConfig(
  val sessionName: String = "Packet Capture Pro TUN",
  val mtu: Int = 1500,
  val snapLength: Int = 65535,
  val virtualIpv4Address: String = "10.1.10.1",
  val ipv4PrefixLength: Int = 24,
  val virtualIpv6Address: String = "fd00:1:fd00:1::1",
  val ipv6PrefixLength: Int = 64,
  val ipv4Route: String = "0.0.0.0",
  val ipv4RoutePrefix: Int = 0,
  val ipv6Route: String = "::",
  val ipv6RoutePrefix: Int = 0,
  val dnsServers: List<String> = listOf("8.8.8.8", "1.1.1.1", "2001:4860:4860::8888"),
  val allowedPackages: List<String> = emptyList(),
  val disallowedPackages: List<String> = emptyList(),
  val isBlocking: Boolean = false,
  val isMetered: Boolean = false,
  val filterExpression: String = ""
) : Serializable

/**
 * Status snapshot of the established TUN virtual interface.
 */
data class TunInterfaceStatus(
  val isEstablished: Boolean = false,
  val interfaceName: String = "tun0",
  val mtu: Int = 1500,
  val assignedIpv4: String = "10.1.10.1/24",
  val assignedIpv6: String = "fd00:1:fd00:1::1/64",
  val activeDnsServers: List<String> = emptyList(),
  val fileDescriptorInt: Int = -1,
  val filterRule: String = "ALL",
  val disallowedAppCount: Int = 1,
  val establishedTimestamp: Long = 0L
) : Serializable
