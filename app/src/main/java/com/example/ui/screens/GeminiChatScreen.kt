package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiModelChoice
import com.example.data.gemini.GroundingCitation
import com.example.data.gemini.MessageRole
import com.example.data.gemini.PromptStarter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GeminiChatScreen(
  messages: List<ChatMessage>,
  isGenerating: Boolean,
  selectedModel: GeminiModelChoice,
  onModelSelected: (GeminiModelChoice) -> Unit,
  onSendMessage: (text: String, attachTelemetry: Boolean) -> Unit,
  onClearChat: () -> Unit,
  liveTelemetrySummary: String
) {
  val context = LocalContext.current
  val listState = rememberLazyListState()
  var inputMessage by remember { mutableStateOf("") }
  var attachTelemetry by remember { mutableStateOf(false) }

  val promptStarters = remember {
    listOf(
      PromptStarter(
        title = "⚡ Protocol Quick Check",
        subtitle = "Sub-second triage",
        prompt = "Explain difference between TLS 1.2 vs 1.3 handshake packet overhead.",
        recommendedModel = GeminiModelChoice.FLASH_LITE
      ),
      PromptStarter(
        title = "🌐 Live Threat & CVE Search",
        subtitle = "Google Search Grounded",
        prompt = "Search latest 2025/2026 security CVEs related to OpenSSL, BIND DNS, and HTTP/2 Rapid Reset attacks.",
        recommendedModel = GeminiModelChoice.FLASH_SEARCH_GROUNDED
      ),
      PromptStarter(
        title = "🧠 Deep Packet Forensics",
        subtitle = "High Thinking Mode",
        prompt = "Perform deep architectural forensic analysis on TCP SYN floods and craft defensive BPF filters.",
        recommendedModel = GeminiModelChoice.PRO_HIGH_THINKING
      ),
      PromptStarter(
        title = "📊 Analyze Active Capture",
        subtitle = "Telemetry Context",
        prompt = "Analyze my current live network capture stats and identify any anomalies or suspicious flows.",
        recommendedModel = GeminiModelChoice.FLASH_SEARCH_GROUNDED
      )
    )
  }

  // Auto-scroll to bottom when messages update
  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("gemini_chat_screen")
  ) {
    // Model Selector & Control Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Cyber AI Analyst",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (messages.isNotEmpty()) {
              IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(32.dp).testTag("clear_chat_button")
              ) {
                Icon(
                  imageVector = Icons.Default.ClearAll,
                  contentDescription = "Clear History",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Three Mode Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          GeminiModelChoice.entries.forEach { model ->
            val isSelected = selectedModel == model
            FilterChip(
              selected = isSelected,
              onClick = { onModelSelected(model) },
              label = {
                Text(
                  text = model.badgeText,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              leadingIcon = {
                val icon = when (model) {
                  GeminiModelChoice.FLASH_LITE -> Icons.Default.Bolt
                  GeminiModelChoice.FLASH_SEARCH_GROUNDED -> Icons.Default.TravelExplore
                  GeminiModelChoice.PRO_HIGH_THINKING -> Icons.Default.Psychology
                }
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = when (model) {
                  GeminiModelChoice.FLASH_LITE -> Color(0xFFE0F2FE)
                  GeminiModelChoice.FLASH_SEARCH_GROUNDED -> Color(0xFFDCFCE7)
                  GeminiModelChoice.PRO_HIGH_THINKING -> Color(0xFFF3E8FF)
                },
                selectedLabelColor = when (model) {
                  GeminiModelChoice.FLASH_LITE -> Color(0xFF0369A1)
                  GeminiModelChoice.FLASH_SEARCH_GROUNDED -> Color(0xFF15803D)
                  GeminiModelChoice.PRO_HIGH_THINKING -> Color(0xFF7E22CE)
                }
              ),
              modifier = Modifier.testTag("model_chip_${model.name}")
            )
          }
        }

        Text(
          text = selectedModel.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          modifier = Modifier.padding(top = 4.dp, start = 2.dp)
        )
      }
    }

    // Chat Message Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item { Spacer(modifier = Modifier.height(6.dp)) }

      // Welcome Banner & Prompt Starters if empty
      if (messages.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Packet Capture Pro Cyber AI",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Real-time protocol forensics & threat intelligence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Ask any network question, analyze raw packet structures, formulate Wireshark filters, or perform live threat queries with Google Search grounding.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Suggested Questions:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(6.dp))

              promptStarters.forEach { starter ->
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      onModelSelected(starter.recommendedModel)
                      inputMessage = starter.prompt
                    },
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        starter.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                      Text(
                        starter.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Message Items
      items(messages, key = { it.id }) { message ->
        ChatMessageBubble(
          message = message,
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("AI Response", message.content))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          onOpenUrl = { url ->
            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
              context.startActivity(intent)
            } catch (_: Exception) {
              Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      // Thinking / Generating Indicator
      if (isGenerating) {
        item {
          GeneratingBubble(selectedModel = selectedModel)
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    // Input Bar Area
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 4.dp,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        // Quick Telemetry Attachment Chip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          AssistChip(
            onClick = { attachTelemetry = !attachTelemetry },
            label = {
              Text(
                if (attachTelemetry) "✓ Live Telemetry Attached" else "+ Attach Live Network Telemetry",
                fontSize = 11.sp
              )
            },
            leadingIcon = {
              Icon(
                if (attachTelemetry) Icons.Default.Check else Icons.Default.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
            },
            colors = AssistChipDefaults.assistChipColors(
              containerColor = if (attachTelemetry) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              labelColor = if (attachTelemetry) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("attach_telemetry_chip")
          )

          Text(
            text = "Model: ${selectedModel.displayName}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Text Input Field & Send Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputMessage,
            onValueChange = { inputMessage = it },
            placeholder = {
              Text(
                when (selectedModel) {
                  GeminiModelChoice.FLASH_LITE -> "Ask fast protocol question..."
                  GeminiModelChoice.FLASH_SEARCH_GROUNDED -> "Search live CVEs or network threats..."
                  GeminiModelChoice.PRO_HIGH_THINKING -> "Ask complex forensic analysis..."
                },
                fontSize = 13.sp
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field"),
            maxLines = 4,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              val text = inputMessage.trim()
              if (text.isNotBlank() && !isGenerating) {
                inputMessage = ""
                onSendMessage(text, attachTelemetry)
                attachTelemetry = false
              }
            },
            enabled = inputMessage.isNotBlank() && !isGenerating,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (inputMessage.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
              .testTag("send_chat_button")
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Send",
              tint = if (inputMessage.isNotBlank() && !isGenerating) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(
  message: ChatMessage,
  onCopy: () -> Unit,
  onOpenUrl: (String) -> Unit
) {
  val isUser = message.role == MessageRole.USER
  val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      modifier = Modifier
        .widthIn(max = 320.dp)
        .testTag("message_card_${message.id}"),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
      ),
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        // Model & Metadata Header (for Assistant messages)
        if (!isUser) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            message.modelUsed?.let { model ->
              Text(
                text = model.badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when (model) {
                  GeminiModelChoice.FLASH_LITE -> Color(0xFF0284C7)
                  GeminiModelChoice.FLASH_SEARCH_GROUNDED -> Color(0xFF16A34A)
                  GeminiModelChoice.PRO_HIGH_THINKING -> Color(0xFF9333EA)
                },
                fontSize = 10.sp
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              message.latencyMs?.let { latency ->
                Text(
                  text = "${latency}ms",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
              }
              IconButton(onClick = onCopy, modifier = Modifier.size(20.dp)) {
                Icon(
                  Icons.Default.ContentCopy,
                  contentDescription = "Copy",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        // Message Content Body
        Text(
          text = message.content,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          lineHeight = 20.sp
        )

        // Thinking process indicator if present
        if (!isUser && message.isThinking) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = Color(0xFFFAF5FF),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE9D5FF))
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = Color(0xFF9333EA),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Reasoned with High Thinking Mode (ThinkingLevel.HIGH)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7E22CE),
                fontSize = 10.sp
              )
            }
          }
        }

        // Google Search Grounding Sources Card
        if (!isUser && message.groundingSources.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            color = Color(0xFFF0FDF4),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.TravelExplore,
                  contentDescription = null,
                  tint = Color(0xFF16A34A),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Grounded with Google Search (${message.groundingSources.size} sources)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF15803D),
                  fontSize = 10.sp
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              message.groundingSources.take(3).forEach { citation ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onOpenUrl(citation.uri) }
                    .padding(vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = citation.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF166534),
                    fontSize = 10.sp,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }

        // Timestamp Footer
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = timeStr,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.align(if (isUser) Alignment.End else Alignment.Start)
        )
      }
    }
  }
}

@Composable
private fun GeneratingBubble(selectedModel: GeminiModelChoice) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.primary
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = when (selectedModel) {
            GeminiModelChoice.FLASH_LITE -> "⚡ Generating low-latency response..."
            GeminiModelChoice.FLASH_SEARCH_GROUNDED -> "🌐 Querying live Google Search grounding..."
            GeminiModelChoice.PRO_HIGH_THINKING -> "🧠 Performing deep packet forensic thinking..."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp
        )
      }
    }
  }
}
