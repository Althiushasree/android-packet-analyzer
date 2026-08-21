package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.ConversationItem
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.DisplayFilterPreset
import com.example.data.model.EndpointItem
import com.example.data.model.EnhancedProtocolAnalysis
import com.example.data.model.IpThreatRisk
import com.example.data.model.PacketEntity
import com.example.data.model.ProtocolDistribution
import com.example.data.model.ProtocolHierarchyNode
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
  enhancedProtocols: List<EnhancedProtocolAnalysis> = emptyList(),
  protocolHierarchy: ProtocolHierarchyNode? = null,
  displayFilters: List<DisplayFilterPreset>,
  onSearchQueryChange: (String) -> Unit,
  onProtocolSelect: (String) -> Unit,
  onPacketClick: (PacketEntity) -> Unit,
  onInspectApp: (DetailedAppTraffic) -> Unit,
  onInspectIp: (DetailedIpTraffic) -> Unit,
  onFilterSelected: (String) -> Unit,
  onNavigateToPackets: (appName: String?, ipAddress: String?, protocol: String?) -> Unit = { _, _, _ -> }
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var selectedIpModal by remember { mutableStateOf<DetailedIpTraffic?>(null) }
  val tabTitles = listOf("Packets", "Protocols", "IP Analyzer", "Applications", "Conversations", "Endpoints", "Filters")

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
        1 -> EnhancedProtocolAnalyzerTab(
          enhancedProtocols = enhancedProtocols,
          protocols = protocols,
          hierarchyRoot = protocolHierarchy,
          onProtocolClick = { protoName ->
            onProtocolSelect(protoName)
            selectedTab = 0
          },
          onExportCsv = {
            val csv = CsvExportUtils.exportProtocolsToCsv(protocols)
            CsvExportUtils.shareCsv(context, csv, "Export Protocol Statistics CSV")
          }
        )
        2 -> EnhancedIpAnalyzerTab(
          ips = detailedIps,
          onSelectIp = { ip -> selectedIpModal = ip },
          onInspectIpPackets = { ipStr ->
            onSearchQueryChange(ipStr)
            selectedTab = 0
          },
          onExportCsv = {
            val csv = CsvExportUtils.exportIpsToCsv(detailedIps)
            CsvExportUtils.shareCsv(context, csv, "Export IP Addresses CSV")
          }
        )
        3 -> ApplicationsTabContent(
          apps = detailedApps,
          onInspectApp = onInspectApp,
          onExportCsv = {
            val csv = CsvExportUtils.exportAppsToCsv(detailedApps)
            CsvExportUtils.shareCsv(context, csv, "Export Applications CSV")
          }
        )
        4 -> ConversationsTabContent(
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
        5 -> EndpointsTabContent(
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

  // Full IP Dossier Modal Dialog
  selectedIpModal?.let { ipDetails ->
    IpIntelligenceDossierDialog(
      ip = ipDetails,
      onDismiss = { selectedIpModal = null },
      onInspectPackets = {
        onSearchQueryChange(ipDetails.ip)
        selectedIpModal = null
        selectedTab = 0
      }
    )
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
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Filter IP, Port, Host, App...", fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("analyze_search_input"),
        shape = RoundedCornerShape(8.dp)
      )

      IconButton(
        onClick = onExportCsv,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Protocol Quick Filter Chips
    val protocols = listOf("ALL", "TCP", "UDP", "TLS", "DNS", "HTTP", "QUIC")
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(protocols) { proto ->
        FilterChip(
          selected = selectedProtocol == proto,
          onClick = { onProtocolSelect(proto) },
          label = { Text(proto, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          shape = RoundedCornerShape(8.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Packet List Table
    if (packets.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(8.dp))
          Text("No captured packets match query", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(items = packets, key = { it.id }) { packet ->
          val isExpanded = expandedState[packet.id] ?: false
          ExpandablePacketRowItem(
            packet = packet,
            isExpanded = isExpanded,
            onToggleExpand = { expandedState[packet.id] = !isExpanded },
            onInspect = { onPacketClick(packet) }
          )
        }
      }
    }
  }
}

@Composable
private fun EnhancedProtocolAnalyzerTab(
  enhancedProtocols: List<EnhancedProtocolAnalysis>,
  protocols: List<ProtocolDistribution>,
  hierarchyRoot: ProtocolHierarchyNode?,
  onProtocolClick: (String) -> Unit,
  onExportCsv: () -> Unit
) {
  var showHierarchyTree by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Deep Protocol Analyzer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Layer 3 / 4 / 7 Protocol Breakdown & Flows", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Row {
        IconButton(onClick = { showHierarchyTree = !showHierarchyTree }) {
          Icon(
            Icons.Default.AccountTree,
            contentDescription = "Toggle Hierarchy Tree",
            tint = if (showHierarchyTree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onExportCsv) {
          Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (showHierarchyTree && hierarchyRoot != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Wireshark Protocol Hierarchy Tree", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          ProtocolHierarchyNodeView(node = hierarchyRoot, depth = 0, onProtocolClick = onProtocolClick)
        }
      }
    }

    // Protocol Cards List
    val displayList = if (enhancedProtocols.isNotEmpty()) {
      enhancedProtocols
    } else {
      protocols.map { p ->
        EnhancedProtocolAnalysis(
          protocol = p.protocol,
          packetCount = p.count,
          totalBytes = p.bytes,
          bytePercentage = p.percentage,
          packetPercentage = p.percentage,
          activeFlows = (p.count / 4).coerceAtLeast(1),
          errorCount = 0,
          errorRatePercent = 0f,
          avgPacketSize = if (p.count > 0) (p.bytes / p.count).toInt() else 0,
          topPorts = listOf(443, 80)
        )
      }
    }

    if (displayList.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No protocol activity recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = displayList, key = { it.protocol }) { proto ->
          ProtocolDetailCard(proto = proto, onClick = { onProtocolClick(proto.protocol) })
        }
      }
    }
  }
}

@Composable
private fun ProtocolHierarchyNodeView(
  node: ProtocolHierarchyNode,
  depth: Int,
  onProtocolClick: (String) -> Unit
) {
  var isExpanded by remember { mutableStateOf(true) }

  Column(modifier = Modifier.padding(start = (depth * 14).dp, top = 2.dp, bottom = 2.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .clickable { if (node.children.isNotEmpty()) isExpanded = !isExpanded else onProtocolClick(node.name) }
        .padding(vertical = 4.dp, horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (node.children.isNotEmpty()) {
          Icon(
            if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        } else {
          Spacer(modifier = Modifier.width(16.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(node.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text("(${node.layer})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
      }

      Text(
        "${node.packetCount} pkts (${String.format(Locale.US, "%.1f", node.byteCount / 1024.0)} KB)",
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.primary
      )
    }

    if (isExpanded && node.children.isNotEmpty()) {
      node.children.forEach { child ->
        ProtocolHierarchyNodeView(node = child, depth = depth + 1, onProtocolClick = onProtocolClick)
      }
    }
  }
}

@Composable
private fun ProtocolDetailCard(
  proto: EnhancedProtocolAnalysis,
  onClick: () -> Unit
) {
  val badgeColor = when (proto.protocol.uppercase()) {
    "TCP" -> ProtocolTcp
    "UDP" -> ProtocolUdp
    "TLS", "HTTPS" -> ProtocolTls
    "DNS" -> ProtocolDns
    "HTTP" -> ProtocolHttp
    "QUIC" -> ProtocolQuic
    else -> ProtocolOther
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = badgeColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = proto.protocol,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = badgeColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = proto.layer,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontSize = 10.sp
            )
          }
        }

        Text(
          text = "${String.format(Locale.US, "%.1f", proto.bytePercentage)}%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      LinearProgressIndicator(
        progress = { (proto.bytePercentage / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = badgeColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
      Spacer(modifier = Modifier.height(10.dp))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
          Text("Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            "${String.format(Locale.US, "%.2f", proto.totalBytes / 1024.0 / 1024.0)} MB",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
        }
        Column {
          Text("Packets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${proto.packetCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Column {
          Text("Flows / Streams", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("${proto.activeFlows}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Column {
          Text("Error Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            "${String.format(Locale.US, "%.1f", proto.errorRatePercent)}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (proto.errorRatePercent > 5f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
          )
        }
      }

      if (proto.topPorts.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Top Ports: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          proto.topPorts.forEach { port ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Text(":$port", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
          }
          Spacer(modifier = Modifier.weight(1f))
          Text(
            text = "Inspect Packets →",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun EnhancedIpAnalyzerTab(
  ips: List<DetailedIpTraffic>,
  onSelectIp: (DetailedIpTraffic) -> Unit,
  onInspectIpPackets: (String) -> Unit,
  onExportCsv: () -> Unit
) {
  var ipSearch by remember { mutableStateOf("") }
  val filteredIps = remember(ips, ipSearch) {
    if (ipSearch.isBlank()) ips else ips.filter {
      it.ip.contains(ipSearch, ignoreCase = true) || it.hostname.contains(ipSearch, ignoreCase = true) || it.asn.contains(ipSearch, ignoreCase = true) || it.country.contains(ipSearch, ignoreCase = true)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("Full IP Intelligence Analyzer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("${ips.size} Observed Remote & Local Endpoints", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = ipSearch,
      onValueChange = { ipSearch = it },
      placeholder = { Text("Search IP, Hostname, ASN, Country...", fontSize = 12.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (filteredIps.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No communicating IP addresses found", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = filteredIps, key = { it.ip }) { ip ->
          IpIntelligenceSummaryCard(
            ip = ip,
            onClick = { onSelectIp(ip) },
            onInspectPackets = { onInspectIpPackets(ip.ip) }
          )
        }
      }
    }
  }
}

@Composable
private fun IpIntelligenceSummaryCard(
  ip: DetailedIpTraffic,
  onClick: () -> Unit,
  onInspectPackets: () -> Unit
) {
  val threatColor = when (ip.threatRisk) {
    IpThreatRisk.CLEAN -> Color(0xFF2E7D32)
    IpThreatRisk.LOW_RISK -> Color(0xFF1565C0)
    IpThreatRisk.MODERATE_RISK -> Color(0xFFF57C00)
    IpThreatRisk.SUSPICIOUS, IpThreatRisk.MALICIOUS -> Color(0xFFC62828)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(ip.ip, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(ip.hostname, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Surface(
          color = threatColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = ip.threatRisk.label.split("/")[0].trim(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = threatColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 10.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
          Text("${ip.countryCode} • ${ip.city}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
          Text(ip.asn.take(24), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "DL: ${String.format(Locale.US, "%.1f", ip.downloadBytes / 1024.0 / 1024.0)}MB • UL: ${String.format(Locale.US, "%.1f", ip.uploadBytes / 1024.0 / 1024.0)}MB (${ip.packetCount} pkts)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Dossier & Packets",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
private fun IpIntelligenceDossierDialog(
  ip: DetailedIpTraffic,
  onDismiss: () -> Unit,
  onInspectPackets: () -> Unit
) {
  val threatColor = when (ip.threatRisk) {
    IpThreatRisk.CLEAN -> Color(0xFF2E7D32)
    IpThreatRisk.LOW_RISK -> Color(0xFF1565C0)
    IpThreatRisk.MODERATE_RISK -> Color(0xFFF57C00)
    IpThreatRisk.SUSPICIOUS, IpThreatRisk.MALICIOUS -> Color(0xFFC62828)
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("IP Intelligence Dossier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Target IP & Hostname
        Text(ip.ip, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(ip.hostname, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        // Threat Reputation Badge
        Surface(
          color = threatColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              if (ip.threatRisk == IpThreatRisk.CLEAN) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = threatColor,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Reputation: ${ip.threatRisk.label}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = threatColor
              )
              Text(
                text = ip.threatNotes,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GeoIP & ASN Info
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(8.dp)
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Location:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${ip.city}, ${ip.region}, ${ip.country} (${ip.countryCode})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Autonomous System:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(ip.asn, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("ISP / Cloud Provider:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(ip.isp, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Est. Latency (RTT):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("${String.format(Locale.US, "%.1f", ip.estimatedRttMs)} ms", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Communicating Apps
        if (ip.communicatingApps.isNotEmpty()) {
          Text("Associated Applications:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ip.communicatingApps.take(3).forEach { app ->
              Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                Text("${app.appName} (${String.format(Locale.US, "%.1f", app.percentage)}%)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 10.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Close")
          }
          Button(
            onClick = onInspectPackets,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Inspect Packets")
          }
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
      Text("Application Traffic Consumption (${apps.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      IconButton(onClick = onExportCsv) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
      }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(items = apps, key = { it.appPackage }) { app ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectApp(app) },
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(app.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text(
                "${String.format(Locale.US, "%.1f", app.percentage)}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${String.format(Locale.US, "%.2f", app.totalBytes / 1024.0 / 1024.0)} MB (${app.packetCount} pkts) • DL: ${String.format(Locale.US, "%.2f", app.downloadBytes / 1024.0 / 1024.0)} MB",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { (app.percentage / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onConversationClick(c) },
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
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onEndpointClick(ep) },
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
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onApplyFilter(preset.filterExpression) },
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