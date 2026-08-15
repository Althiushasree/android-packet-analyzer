package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BandwidthTestResult
import com.example.data.model.DnsRecord
import com.example.data.model.PingHopResult
import com.example.data.model.PortScanResult
import com.example.data.model.TracerouteHop
import java.util.Locale

@Composable
fun ToolsScreen(
  pingResults: List<PingHopResult>,
  isPingRunning: Boolean,
  tracerouteResults: List<TracerouteHop>,
  isTracerouteRunning: Boolean,
  dnsResults: List<DnsRecord>,
  isDnsRunning: Boolean,
  portScanResults: List<PortScanResult>,
  isPortScanRunning: Boolean,
  bandwidthResult: BandwidthTestResult,
  packetGenLog: List<String>,
  isPacketGenRunning: Boolean,
  onRunPing: (String, Int) -> Unit,
  onRunTraceroute: (String) -> Unit,
  onRunDns: (String) -> Unit,
  onRunPortScan: (String, Int, Int) -> Unit,
  onRunBandwidthTest: () -> Unit,
  onRunPacketGen: (String, Int, String, Int, String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val toolTitles = listOf("Ping", "Traceroute", "DNS Lookup", "Port Scan", "Speed Test", "Packet Crafter")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("tools_screen")
  ) {
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 12.dp
    ) {
      toolTitles.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      when (selectedTab) {
        0 -> PingToolTab(
          results = pingResults,
          isRunning = isPingRunning,
          onRun = onRunPing
        )
        1 -> TracerouteToolTab(
          results = tracerouteResults,
          isRunning = isTracerouteRunning,
          onRun = onRunTraceroute
        )
        2 -> DnsLookupToolTab(
          results = dnsResults,
          isRunning = isDnsRunning,
          onRun = onRunDns
        )
        3 -> PortScanToolTab(
          results = portScanResults,
          isRunning = isPortScanRunning,
          onRun = onRunPortScan
        )
        4 -> SpeedTestToolTab(
          result = bandwidthResult,
          onRun = onRunBandwidthTest
        )
        5 -> PacketCrafterToolTab(
          logs = packetGenLog,
          isRunning = isPacketGenRunning,
          onRun = onRunPacketGen
        )
      }
    }
  }
}

@Composable
private fun PingToolTab(
  results: List<PingHopResult>,
  isRunning: Boolean,
  onRun: (String, Int) -> Unit
) {
  var host by remember { mutableStateOf("1.1.1.1") }
  var count by remember { mutableStateOf("4") }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text("ICMP / UDP Echo Ping Diagnostic", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = host,
        onValueChange = { host = it },
        label = { Text("Target Host / IP") },
        modifier = Modifier.weight(2f),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
      )
      OutlinedTextField(
        value = count,
        onValueChange = { count = it },
        label = { Text("Count") },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = { onRun(host, count.toIntOrNull() ?: 4) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isRunning && host.isNotBlank(),
      shape = RoundedCornerShape(10.dp)
    ) {
      if (isRunning) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Pinging Target...")
      } else {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("START PING")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Ping Execution Log", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (results.isEmpty() && !isRunning) {
          Text("No ping output recorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          results.forEach { r ->
            Text(
              text = "${r.bytes} bytes from ${r.ip}: icmp_seq=${r.seq} ttl=${r.ttl} time=${String.format(Locale.US, "%.2f", r.rttMs)} ms",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFF16A34A),
              modifier = Modifier.padding(vertical = 2.dp)
            )
          }

          if (results.isNotEmpty() && !isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            val avg = results.map { it.rttMs }.average()
            val min = results.minOf { it.rttMs }
            val max = results.maxOf { it.rttMs }
            Text(
              text = "rtt min/avg/max = ${String.format(Locale.US, "%.1f/%.1f/%.1f", min, avg, max)} ms (0% packet loss)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TracerouteToolTab(
  results: List<TracerouteHop>,
  isRunning: Boolean,
  onRun: (String) -> Unit
) {
  var host by remember { mutableStateOf("8.8.8.8") }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text("Layer 3 Route Hop-by-Hop Traceroute", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = host,
      onValueChange = { host = it },
      label = { Text("Target Host / Domain") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = { onRun(host) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isRunning && host.isNotBlank(),
      shape = RoundedCornerShape(10.dp)
    ) {
      if (isRunning) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Tracing Route...")
      } else {
        Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("TRACE ROUTE")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Hop Sequence", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (results.isEmpty() && !isRunning) {
          Text("No traceroute data available", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          results.forEach { h ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("${h.hop}. ${h.ip} (${h.host})", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
              Text("${String.format(Locale.US, "%.1f", h.rtt1Ms)} ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DnsLookupToolTab(
  results: List<DnsRecord>,
  isRunning: Boolean,
  onRun: (String) -> Unit
) {
  var domain by remember { mutableStateOf("google.com") }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Text("DNS Name Resolution & Records Query", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = domain,
      onValueChange = { domain = it },
      label = { Text("Domain Name") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = { onRun(domain) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isRunning && domain.isNotBlank(),
      shape = RoundedCornerShape(10.dp)
    ) {
      if (isRunning) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Querying DNS Server...")
      } else {
        Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("RESOLVE RECORDS")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Resolved DNS Records", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (results.isEmpty() && !isRunning) {
          Text("No DNS records resolved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          results.forEach { r ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("${r.type} Record:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              Text(r.value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PortScanToolTab(
  results: List<PortScanResult>,
  isRunning: Boolean,
  onRun: (String, Int, Int) -> Unit
) {
  var host by remember { mutableStateOf("127.0.0.1") }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    // Authorized Safety Warning Banner
    Surface(
      color = Color(0xFFFEF2F2),
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "Notice: Only scan networks and hosts you are explicitly authorized to audit.",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF991B1B)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text("TCP Socket Port Scanner", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = host,
      onValueChange = { host = it },
      label = { Text("Target IP / Hostname") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = { onRun(host, 20, 1000) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isRunning && host.isNotBlank(),
      shape = RoundedCornerShape(10.dp)
    ) {
      if (isRunning) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scanning Common Ports...")
      } else {
        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("SCAN COMMON PORTS")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Port Audit Results", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (results.isEmpty() && !isRunning) {
          Text("No ports scanned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          results.forEach { p ->
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (p.isOpen) Color(0xFF16A34A) else Color(0xFF94A3B8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Port ${p.port} (${p.serviceName})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
              }
              Text(
                if (p.isOpen) "OPEN (${p.responseTimeMs}ms)" else "CLOSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (p.isOpen) Color(0xFF16A34A) else Color(0xFF64748B)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SpeedTestToolTab(
  result: BandwidthTestResult,
  onRun: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Active Bandwidth & Latency Speed Test", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    // Speed Gauges
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("DOWNLOAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(4.dp))
          Text("${String.format(Locale.US, "%.1f", result.downloadMbps)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
          Text("Mbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("UPLOAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(4.dp))
          Text("${String.format(Locale.US, "%.1f", result.uploadMbps)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
          Text("Mbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Ping", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${String.format(Locale.US, "%.1f", result.pingMs)} ms", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
      }
      Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Jitter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${String.format(Locale.US, "%.1f", result.jitterMs)} ms", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (result.isRunning) {
      LinearProgressIndicator(
        progress = { result.progress },
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
      )
      Spacer(modifier = Modifier.height(12.dp))
    }

    Button(
      onClick = onRun,
      modifier = Modifier.fillMaxWidth(),
      enabled = !result.isRunning,
      shape = RoundedCornerShape(10.dp)
    ) {
      Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(if (result.isRunning) "TESTING IN PROGRESS..." else "START SPEED TEST")
    }
  }
}

@Composable
private fun PacketCrafterToolTab(
  logs: List<String>,
  isRunning: Boolean,
  onRun: (String, Int, String, Int, String) -> Unit
) {
  var targetIp by remember { mutableStateOf("127.0.0.1") }
  var port by remember { mutableStateOf("8080") }
  var protocol by remember { mutableStateOf("UDP") }
  var count by remember { mutableStateOf("5") }
  var payload by remember { mutableStateOf("PACKET_CAPTURE_PRO_TEST_PAYLOAD") }

  Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
    Surface(
      color = Color(0xFFFEF3C7),
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          "Safety Simulation: Packet generator tests protocol stack handling in local loopback.",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF92400E)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = targetIp,
        onValueChange = { targetIp = it },
        label = { Text("Target IP") },
        modifier = Modifier.weight(2f),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
      )
      OutlinedTextField(
        value = port,
        onValueChange = { port = it },
        label = { Text("Port") },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = payload,
      onValueChange = { payload = it },
      label = { Text("Custom ASCII Payload") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = { onRun(targetIp, port.toIntOrNull() ?: 80, protocol, count.toIntOrNull() ?: 5, payload) },
      modifier = Modifier.fillMaxWidth(),
      enabled = !isRunning && targetIp.isNotBlank(),
      shape = RoundedCornerShape(10.dp)
    ) {
      Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(if (isRunning) "DISPATCHING PACKETS..." else "DISPATCH TEST PACKETS")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Transmission Log", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
          Text("No packet crafting activity dispatched", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
          logs.forEach { log ->
            Text(log, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 2.dp))
          }
        }
      }
    }
  }
}
