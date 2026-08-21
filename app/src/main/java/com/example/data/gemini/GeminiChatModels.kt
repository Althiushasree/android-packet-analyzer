package com.example.data.gemini

import java.io.Serializable
import java.util.UUID

/**
 * Supported Gemini AI Models for Packet Capture Pro.
 */
enum class GeminiModelChoice(
  val modelId: String,
  val displayName: String,
  val badgeText: String,
  val description: String
) {
  FLASH_LITE(
    modelId = "gemini-3.1-flash-lite",
    displayName = "Gemini Flash Lite",
    badgeText = "⚡ Ultra Fast",
    description = "Sub-second low-latency triage and quick packet protocol queries."
  ),
  FLASH_SEARCH_GROUNDED(
    modelId = "gemini-3.5-flash",
    displayName = "Gemini 3.5 Flash + Search",
    badgeText = "🌐 Live Search",
    description = "General network analysis with Google Search Grounding for live threat intel and CVEs."
  ),
  PRO_HIGH_THINKING(
    modelId = "gemini-3.1-pro-preview",
    displayName = "Gemini 3.1 Pro (High Thinking)",
    badgeText = "🧠 High Thinking",
    description = "Deep multi-step reasoning with ThinkingLevel.HIGH for complex forensic dissection."
  )
}

/**
 * Web search citation/source returned by Google Search Grounding.
 */
data class GroundingCitation(
  val title: String,
  val uri: String,
  val snippet: String = ""
) : Serializable

/**
 * Represents a single message in the Gemini multi-turn conversation thread.
 */
data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val role: MessageRole,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val modelUsed: GeminiModelChoice? = null,
  val isThinking: Boolean = false,
  val thinkingText: String? = null,
  val groundingSources: List<GroundingCitation> = emptyList(),
  val searchQueries: List<String> = emptyList(),
  val latencyMs: Long? = null,
  val isError: Boolean = false
) : Serializable

enum class MessageRole {
  USER,
  MODEL,
  SYSTEM
}

/**
 * Suggested prompt starter for quick network analysis.
 */
data class PromptStarter(
  val title: String,
  val subtitle: String,
  val prompt: String,
  val recommendedModel: GeminiModelChoice
)
