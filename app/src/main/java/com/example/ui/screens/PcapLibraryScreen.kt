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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PcapFileEntity
import kotlinx.coroutines.launch

@Composable
fun PcapLibraryScreen(
  pcapFiles: List<PcapFileEntity>,
  onExportCurrentCapture: (String) -> Unit,
  onDeletePcap: (Long) -> Unit,
  onSelectPcapForHexView: (PcapFileEntity) -> Unit
) {
  var showExportDialog by remember { mutableStateOf(false) }
  var exportNotes by remember { mutableStateOf("") }
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("pcap_library_screen")
  ) {
    // Header Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
      shape = RoundedCornerShape(16.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "On-Device PCAP Library",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "${pcapFiles.size} Saved Captures available for analysis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }

        Button(
          onClick = { showExportDialog = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("save_export_pcap_button")
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save PCAP")
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Saved Captures",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (pcapFiles.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Text("No PCAP files saved yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(
          items = pcapFiles,
          key = { it.id }
        ) { pcap ->
          PcapCardItem(
            pcap = pcap,
            onInspect = { onSelectPcapForHexView(pcap) },
            onDelete = { onDeletePcap(pcap.id) }
          )
        }
      }
    }

    // Save Capture Dialog
    if (showExportDialog) {
      Dialog(onDismissRequest = { showExportDialog = false }) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("export_pcap_dialog"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Export Session to PCAP",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Save all captured packets to a standard Wireshark-compatible PCAP format.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = exportNotes,
              onValueChange = { exportNotes = it },
              label = { Text("Notes / Label (Optional)") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("pcap_notes_input"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { showExportDialog = false }) {
                Text("Cancel")
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  onExportCurrentCapture(exportNotes)
                  showExportDialog = false
                  exportNotes = ""
                },
                modifier = Modifier.testTag("confirm_save_pcap_button")
              ) {
                Text("Export & Save")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PcapCardItem(
  pcap: PcapFileEntity,
  onInspect: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("pcap_item_${pcap.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Description,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = pcap.fileName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${pcap.fileSizeFormatted} • ${pcap.packetCount} packets • ${pcap.dateFormatted}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (pcap.notes.isNotEmpty()) {
          Text(
            text = "Notes: ${pcap.notes}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "Menu")
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Inspect Hex Detail") },
            onClick = {
              showMenu = false
              onInspect()
            },
            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) }
          )
          DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
              showMenu = false
              onDelete()
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
          )
        }
      }
    }
  }
}
