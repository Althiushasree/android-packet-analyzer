package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationItem
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.DisplayFilterPreset
import com.example.data.model.EndpointItem
import com.example.data.model.PacketEntity
import com.example.data.model.ProtocolDistribution
import com.example.ui.components.ExpandablePacketRowItem
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp
import com.example.util.CsvExportUtils
import java.util.Locale

@Composable
fun AnalyzeScreen(
  packets: List<PacketEntity>,
  searchQuery: String,
  selectedProtocol: String,
  detailedApps: List<DetailedAppTraffic>,
  detailedIps: List<DetailedIpTraffic>,
  conversations: List<ConversationItem>,
  endpoints: List<EndpointItem>,
  protocols: List<ProtocolDistribution>,
  displayFilters: List<DisplayFilterPreset>,
  onSearchQueryChange: (String) -> Unit,
  onProtocolSelect: (String) -> Unit,
  onPacketClick: (PacketEntity) -> Unit,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onInspectIp: (DetailedIpTraffic) -> Unit,
  onFilterSelected: (String) -> Unit
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("Packets", "Applications", "IPs", "Conversations", "Endpoints", "Protocols", "Filters")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("analyze_screen")
  ) {
    // Top Scrollable Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary,
      edgePadding = 12.dp
    ) {
      tabTitles.forEachIndexed { index, title ->
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
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      when (selectedTab) {
        0 -> PacketsTabContent(
          packets = packets,
          searchQuery = searchQuery,
          selectedProtocol = selectedProtocol,
          onSearchQueryChange = onSearchQueryChange,
          onProtocolSelect = onProtocolSelect,
          onPacketClick = onPacketClick,
          onExportCsv = {
            val csv = CsvExportUtils.exportPacketsToCsv(packets)
            CsvExportUtils.shareCsv(context, csv, "Export Packets CSV")
          }
        )
        1 -> ApplicationsTabContent(
          apps = detailedApps,
          onInspectApp = onInspectApp,
          onExportCsv = {
            val csv = CsvExportUtils.exportAppsToCsv(detailedApps)
            CsvExportUtils.shareCsv(context, csv, "Export Applications CSV")
          }
        )
        2 -> IpsTabContent(
          ips = detailedIps,
          onInspectIp = onInspectIp,
          onExportCsv = {
            val csv = CsvExportUtils.exportIpsToCsv(detailedIps)
            CsvExportUtils.shareCsv(context, csv, "Export IP Addresses CSV")
          }
        )
        3 -> ConversationsTabContent(
          conversations = conversations,
          onConversationClick = { c ->
            onSearchQueryChange(c.destIp)
            selectedTab = 0
          },
          onExportCsv = {
            val csv = CsvExportUtils.exportConversationsToCsv(conversations)
            CsvExportUtils.shareCsv(context, csv, "Export Conversations CSV")
          }
        )
        4 -> EndpointsTabContent(
          endpoints = endpoints,
          onEndpointClick = { e ->
            onSearchQueryChange(e.address)
            selectedTab = 0
          },
          onExportCsv = {
            val csv = CsvExportUtils.exportEndpointsToCsv(endpoints)
            CsvExportUtils.shareCsv(context, csv, "Export Endpoints CSV")
          }
        )
        5 -> ProtocolsTabContent(
          protocols = protocols,
          onProtocolClick = { p ->
            onProtocolSelect(p.protocol)
            selectedTab = 0
          },
          onExportCsv = {
            val csv = CsvExportUtils.exportProtocolsToCsv(protocols)
            CsvExportUtils.shareCsv(context, csv, "Export Protocol Statistics CSV")
          }
        )
        6 -> DisplayFiltersTabContent(
          presets = displayFilters,
          onApplyFilter = { expr ->
            onFilterSelected(expr)
            onSearchQueryChange(expr)
            selectedTab = 0
          }
        )
      }
    }
  }
}

@Composable
private fun PacketsTabContent(
  packets: List<PacketEntity>,
  searchQuery: String,
  selectedProtocol: String,
  onSearchQueryChange: (String) -> Unit,
  onProtocolSelect: (String) -> Unit,
  onPacketClick: (PacketEntity) -> Unit,
  onExportCsv: () -> Unit
) {
  val expandedState = remember { mutableStateMapOf<Long, Boolean>() }

  Column(modifier = Modifier.fillMaxSize()) {
    // Search Bar & Export CSV Row
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier.weight(1f).testTag("packet_search_input"),
        placeholder = { Text("Filter IP, port, host...", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Protocol Quick Chips
    val protocolsList = listOf("ALL", "TCP", "UDP", "DNS", "TLS", "HTTP", "QUIC")
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(protocolsList) { proto ->
        FilterChip(
          selected = selectedProtocol == proto,
          onClick = { onProtocolSelect(proto) },
          label = { Text(proto, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (packets.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No matching packets captured", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(items = packets, key = { it.id }) { packet ->
          val isExpanded = expandedState[packet.id] == true
          ExpandablePacketRowItem(
            packet = packet,
            isExpanded = isExpanded,
            onToggleExpand = { expandedState[packet.id] = !isExpanded },
            onInspect = { onPacketClick(packet) },
            onFilterIp = { ip -> onSearchQueryChange(ip) }
          )
        }
      }
    }
  }
}

@Composable
private fun ApplicationsTabContent(
  apps: List<DetailedAppTraffic>,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onExportCsv: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Top Traffic Applications (${apps.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = apps, key = { it.appPackage }) { app ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onInspectApp(app) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
              Text(app.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text(app.appPackage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${String.format(Locale.US, "%.2f", app.totalBytes / 1024.0 / 1024.0)} MB (${app.packetCount} pkts) • ${String.format(Locale.US, "%.1f", app.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
private fun IpsTabContent(
  ips: List<DetailedIpTraffic>,
  onInspectIp: (DetailedIpTraffic) -> Unit,
  onExportCsv: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Communicating IP Addresses (${ips.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = ips, key = { it.ip }) { ip ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onInspectIp(ip) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
              Text(ip.ip, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Text(ip.hostname, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${String.format(Locale.US, "%.2f", ip.totalBytes / 1024.0 / 1024.0)} MB (${ip.packetCount} pkts) • ${ip.country}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
private fun ConversationsTabContent(
  conversations: List<ConversationItem>,
  onConversationClick: (ConversationItem) -> Unit,
  onExportCsv: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Active TCP/UDP Conversations (${conversations.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = conversations, key = { it.id }) { c ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onConversationClick(c) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = "${c.sourceIp}:${c.sourcePort} ↔ ${c.destIp}:${c.destPort}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(c.protocol, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = "${c.packetCount} packets • ${String.format(Locale.US, "%.1f", c.totalBytes / 1024.0)} KB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${c.appName} • ${String.format(Locale.US, "%.1f", c.durationSeconds)}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EndpointsTabContent(
  endpoints: List<EndpointItem>,
  onEndpointClick: (EndpointItem) -> Unit,
  onExportCsv: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Network Endpoints (${endpoints.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = endpoints, key = { it.address }) { ep ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onEndpointClick(ep) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
              Text(ep.address, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Text("${ep.hostname} (${ep.type})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${String.format(Locale.US, "%.1f", ep.totalBytes / 1024.0)} KB • Sent: ${String.format(Locale.US, "%.1f", ep.sentBytes / 1024.0)} KB • Recv: ${String.format(Locale.US, "%.1f", ep.receivedBytes / 1024.0)} KB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
private fun ProtocolsTabContent(
  protocols: List<ProtocolDistribution>,
  onProtocolClick: (ProtocolDistribution) -> Unit,
  onExportCsv: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Protocol Hierarchy (${protocols.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = protocols, key = { it.protocol }) { proto ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onProtocolClick(proto) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(proto.protocol, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text(
                "${proto.count} packets captured",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              "${String.format(Locale.US, "%.1f", proto.percentage)}%",
              style = MaterialTheme.typography.titleMedium,
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
private fun DisplayFiltersTabContent(
  presets: List<DisplayFilterPreset>,
  onApplyFilter: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Text("Wireshark Display Filter Presets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = presets, key = { it.name }) { preset ->
        Card(
          modifier = Modifier.fillMaxWidth().clickable { onApplyFilter(preset.filterExpression) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(preset.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Apply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = preset.filterExpression,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = preset.description,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
