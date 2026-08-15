package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PacketEntity
import com.example.ui.theme.ProtocolDns
import com.example.ui.theme.ProtocolHttp
import com.example.ui.theme.ProtocolOther
import com.example.ui.theme.ProtocolQuic
import com.example.ui.theme.ProtocolTcp
import com.example.ui.theme.ProtocolTls
import com.example.ui.theme.ProtocolUdp

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.example.ui.components.ExpandablePacketRowItem

@Composable
fun PacketAnalyzerScreen(
  packets: List<PacketEntity>,
  searchQuery: String,
  selectedProtocol: String,
  onSearchQueryChange: (String) -> Unit,
  onProtocolSelect: (String) -> Unit,
  onPacketClick: (PacketEntity) -> Unit
) {
  val listState = rememberLazyListState()
  val expandedState = remember { mutableStateMapOf<Long, Boolean>() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("packet_analyzer_screen")
  ) {
    // Search Bar Input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("packet_search_input"),
      placeholder = { Text("Filter by IP, port, host or protocol...", fontSize = 14.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchQueryChange("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Protocol Quick Filter Chips
    val protocolsList = listOf("ALL", "TCP", "UDP", "DNS", "TLS", "HTTP", "QUIC")
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(protocolsList) { proto ->
        FilterChip(
          selected = selectedProtocol == proto,
          onClick = { onProtocolSelect(proto) },
          label = { Text(proto, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
          modifier = Modifier.testTag("protocol_chip_$proto"),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Packet Count Subheader
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${packets.size} Packets Filtered",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = "Tap row to expand metadata",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Extensive Infinite Scrolling List with Expandable Rows
    if (packets.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No matching packets found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().testTag("packet_lazy_list")
      ) {
        items(
          items = packets,
          key = { it.id }
        ) { packet ->
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
