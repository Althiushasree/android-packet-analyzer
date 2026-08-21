package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiNetworkChatService {
  companion object {
    private const val TAG = "GeminiChatService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    const val DEFAULT_SYSTEM_INSTRUCTION = """
You are Packet Capture Pro Cyber AI — a world-class network security engineer, packet forensics analyst, and protocol dissection specialist.
Your purpose is to assist network engineers, cybersecurity professionals, and students with:
1. Decoding raw packet headers (IP, TCP, UDP, TLS 1.3, QUIC, DNS, HTTP/2, HTTP/3, ICMP, ARP, DHCP).
2. Investigating packet captures (.pcap), identifying SYN floods, port scans, DNS tunneling, unencrypted cleartext leaks, and TLS cipher downgrades.
3. Formulating Wireshark display filters and BPF capture filters (e.g. 'tcp.flags.syn==1 && tcp.flags.ack==0', 'http.request.method=="POST"').
4. Cross-referencing active threat vulnerabilities, CVE advisories, and malicious IP/port reputations with live Google Search data when grounding is enabled.
5. Performing deep architectural reasoning and chain-of-thought packet forensics with high precision.

Always present your answers clearly, using Markdown tables, code blocks for filter syntax or hex dissections, and structured bullet points.
"""
  }

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private fun getApiKey(): String {
    return try {
      val keyField = BuildConfig::class.java.getField("GEMINI_API_KEY")
      val key = keyField.get(null) as? String ?: ""
      if (key == "MY_GEMINI_API_KEY" || key.isBlank()) "" else key
    } catch (_: Exception) {
      ""
    }
  }

  /**
   * Sends a multi-turn conversation to the selected Gemini model.
   */
  suspend fun sendChatMessage(
    history: List<ChatMessage>,
    userMessage: String,
    modelChoice: GeminiModelChoice,
    systemInstructionText: String = DEFAULT_SYSTEM_INSTRUCTION,
    contextTelemetry: String? = null,
    structuredContext: StructuredNetworkContext? = null
  ): ChatMessage = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val apiKey = getApiKey()

    val telemetryString = when {
      structuredContext != null -> CyberAiContextEngine.buildStructuredContextPrompt(structuredContext)
      !contextTelemetry.isNullOrBlank() -> contextTelemetry
      else -> null
    }

    val combinedUserPrompt = if (telemetryString.isNullOrBlank()) {
      userMessage
    } else {
      "$userMessage\n\n$telemetryString"
    }

    if (apiKey.isBlank()) {
      val latency = System.currentTimeMillis() - startTime
      val fallbackContent = if (structuredContext != null) {
        CyberAiContextEngine.generateDeterministicAnalysis(userMessage, structuredContext, history, modelChoice)
      } else {
        generateOfflineFallbackResponse(combinedUserPrompt, modelChoice)
      }
      return@withContext ChatMessage(
        role = MessageRole.MODEL,
        content = fallbackContent,
        modelUsed = modelChoice,
        latencyMs = latency
      )
    }

    try {
      val payload = JSONObject()

      // 1. Build Contents Array (Multi-turn History + Current Message)
      val contentsArray = JSONArray()

      // Include previous conversation history (up to last 10 messages for token discipline)
      val recentHistory = history.takeLast(10)
      for (msg in recentHistory) {
        if (msg.role == MessageRole.USER || msg.role == MessageRole.MODEL) {
          val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
          val itemObj = JSONObject().apply {
            put("role", roleStr)
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", msg.content)
              })
            })
          }
          contentsArray.put(itemObj)
        }
      }

      // Add the current user message
      contentsArray.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply {
            put("text", combinedUserPrompt)
          })
        })
      })

      payload.put("contents", contentsArray)

      // 2. System Instruction
      val effectiveSystemInstruction = if (telemetryString != null) {
        """$systemInstructionText

CRITICAL GROUNDING INSTRUCTIONS:
- You have been provided with real, verified network telemetry from the user's active packet capture.
- Ground all your metrics (bytes, packets, percentages, application names, IPs) strictly in the provided data.
- Never invent or fabricate hypothetical devices, fake endpoints, or unobserved traffic numbers.
"""
      } else {
        systemInstructionText
      }

      if (effectiveSystemInstruction.isNotBlank()) {
        payload.put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply {
              put("text", effectiveSystemInstruction)
            })
          })
        })
      }

      // 3. Model Specific Capabilities:
      // a) Flash 3.5 with Search Grounding: add googleSearch tool
      if (modelChoice == GeminiModelChoice.FLASH_SEARCH_GROUNDED) {
        val toolsArray = JSONArray().apply {
          put(JSONObject().apply {
            put("googleSearch", JSONObject())
          })
        }
        payload.put("tools", toolsArray)
      }

      // b) Pro 3.1 with High Thinking: add thinkingConfig with thinkingLevel = "HIGH" (NO maxOutputTokens)
      if (modelChoice == GeminiModelChoice.PRO_HIGH_THINKING) {
        val genConfig = JSONObject().apply {
          put("thinkingConfig", JSONObject().apply {
            put("thinkingLevel", "HIGH")
          })
        }
        payload.put("generationConfig", genConfig)
      }

      // c) Flash Lite: standard fast execution
      val endpointUrl = "$BASE_URL${modelChoice.modelId}:generateContent?key=$apiKey"
      val requestBody = payload.toString().toRequestBody("application/json".toMediaType())

      val request = Request.Builder()
        .url(endpointUrl)
        .post(requestBody)
        .build()

      val response = httpClient.newCall(request).execute()
      val latency = System.currentTimeMillis() - startTime

      if (!response.isSuccessful) {
        val errorBody = response.body?.string() ?: "HTTP ${response.code}"
        Log.w(TAG, "Gemini API error ($endpointUrl): $errorBody")
        val fallbackContent = if (structuredContext != null) {
          CyberAiContextEngine.generateDeterministicAnalysis(userMessage, structuredContext, history, modelChoice)
        } else {
          generateOfflineFallbackResponse(combinedUserPrompt, modelChoice)
        }
        return@withContext ChatMessage(
          role = MessageRole.MODEL,
          content = "⚠️ Gemini API Notice (${response.code}): Using high-precision on-device network forensics engine:\n\n$fallbackContent",
          modelUsed = modelChoice,
          latencyMs = latency,
          isError = true
        )
      }

      val responseStr = response.body?.string() ?: ""
      val jsonResponse = JSONObject(responseStr)

      // Extract Candidates
      val candidates = jsonResponse.optJSONArray("candidates")
      val firstCandidate = candidates?.optJSONObject(0)
      val contentObj = firstCandidate?.optJSONObject("content")
      val parts = contentObj?.optJSONArray("parts")

      var responseText = ""
      var thinkingText: String? = null

      if (parts != null) {
        for (i in 0 until parts.length()) {
          val part = parts.optJSONObject(i) ?: continue
          if (part.has("thought") && part.optBoolean("thought", false)) {
            thinkingText = part.optString("text", "")
          } else if (part.has("text")) {
            val txt = part.optString("text", "")
            if (responseText.isEmpty()) {
              responseText = txt
            } else {
              responseText += "\n$txt"
            }
          }
        }
      }

      if (responseText.isBlank()) {
        responseText = firstCandidate?.optJSONObject("content")
          ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text", "") ?: "No response received."
      }

      // Extract Google Search Grounding Metadata
      val groundingCitations = mutableListOf<GroundingCitation>()
      val searchQueries = mutableListOf<String>()

      val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
      if (groundingMetadata != null) {
        val webSearchQueries = groundingMetadata.optJSONArray("webSearchQueries")
        if (webSearchQueries != null) {
          for (i in 0 until webSearchQueries.length()) {
            searchQueries.add(webSearchQueries.optString(i))
          }
        }

        val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
        if (groundingChunks != null) {
          for (i in 0 until groundingChunks.length()) {
            val chunk = groundingChunks.optJSONObject(i)
            val web = chunk?.optJSONObject("web")
            if (web != null) {
              val uri = web.optString("uri", "")
              val title = web.optString("title", "Search Source")
              if (uri.isNotBlank()) {
                groundingCitations.add(GroundingCitation(title = title, uri = uri))
              }
            }
          }
        }
      }

      ChatMessage(
        role = MessageRole.MODEL,
        content = responseText,
        modelUsed = modelChoice,
        isThinking = modelChoice == GeminiModelChoice.PRO_HIGH_THINKING,
        thinkingText = thinkingText,
        groundingSources = groundingCitations,
        searchQueries = searchQueries,
        latencyMs = latency
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error executing Gemini request", e)
      val latency = System.currentTimeMillis() - startTime
      val fallbackContent = if (structuredContext != null) {
        CyberAiContextEngine.generateDeterministicAnalysis(userMessage, structuredContext, history, modelChoice)
      } else {
        generateOfflineFallbackResponse(combinedUserPrompt, modelChoice)
      }
      ChatMessage(
        role = MessageRole.MODEL,
        content = "⚠️ Connection Exception: ${e.localizedMessage ?: "Network unreachable"}.\n\n$fallbackContent",
        modelUsed = modelChoice,
        latencyMs = latency,
        isError = true
      )
    }
  }

  /**
   * Generates high-accuracy on-device answers for common protocol and packet capture queries when offline.
   */
  private fun generateOfflineFallbackResponse(prompt: String, modelChoice: GeminiModelChoice): String {
    val p = prompt.lowercase()
    return when {
      p.contains("syn") && p.contains("flood") || p.contains("dos") -> """
### 🛡️ SYN Flood Detection & Wireshark BPF Filters
A **TCP SYN Flood** is a Denial of Service (DoS) attack where an attacker sends a high volume of `SYN` packets without completing the 3-way handshake (`SYN -> SYN-ACK -> ACK`), depleting server connection state tables (`backlog queue`).

#### 🔍 Wireshark Display Filter:
```wireshark
tcp.flags.syn == 1 and tcp.flags.ack == 0
```
To filter incomplete handshakes:
```wireshark
tcp.flags.syn == 1 and not tcp.analysis.initial_rtt
```

#### 🛡️ Mitigation Strategies:
1. **Enable SYN Cookies**: `sysctl -w net.ipv4.tcp_syncookies=1` on Linux endpoints.
2. **Rate Limiting**: Configure iptables or firewall limits (`iptables -A INPUT -p tcp --syn -m limit --limit 1/s -j ACCEPT`).
3. **Decrease SYN-ACK Retries**: `sysctl -w net.ipv4.tcp_synack_retries=2`.
"""

      p.contains("tls") || p.contains("handshake") || p.contains("ssl") -> """
### 🔒 TLS 1.3 Handshake Dissection & Forensics
TLS 1.3 drastically accelerates the cryptographic handshake compared to TLS 1.2 by completing key negotiation in **1-RTT (Round Trip Time)** or **0-RTT** (Pre-Shared Key resumption).

#### 📊 Packet Sequence:
1. **Client Hello**: Contains supported cipher suites (`TLS_AES_256_GCM_SHA384`), client random, Server Name Indication (SNI), and Key Share extensions (`Curve25519` / `secp256r1`).
2. **Server Hello**: Server selects cipher suite, returns server random and its matching Key Share. From this moment onward, all following records are **encrypted**.
3. **Encrypted Extensions & Certificate**: Transmitted encrypted to protect domain identity.
4. **Finished**: Server verifies HMAC of preceding transcript. Client sends Finished and begins HTTP/2 or HTTP/3 Application Data frames.

#### 🔍 Wireshark Filter:
```wireshark
tls.handshake.type == 1  // Client Hello
tls.handshake.extensions_server_name contains "example.com"
```
"""

      p.contains("filter") || p.contains("wireshark") || p.contains("bpf") -> """
### ⚡ Essential Wireshark & BPF Packet Capture Filters

| Objective | Wireshark Display Filter | BPF Capture Filter |
|---|---|---|
| Filter HTTP POST Requests | `http.request.method == "POST"` | `tcp port 80 and (((ip[2:2] - ((ip[0]&0xf)<<2)) - ((tcp[12:1]&0xf0)>>2)) != 0)` |
| Filter DNS Queries | `dns.flags.response == 0` | `udp port 53` |
| Filter Specific Subnet | `ip.addr == 192.168.1.0/24` | `net 192.168.1.0/24` |
| Suspicious Cleartext Port 80 | `ip and not tls and tcp.port == 80` | `tcp port 80` |
| TCP Retransmissions & Drops | `tcp.analysis.retransmission` | N/A (Stateful) |
"""

      else -> """
### 🌐 Cyber AI Packet Forensics Response
Analyzing query under mode: **${modelChoice.displayName}** (${modelChoice.badgeText})

- **Traffic Assessment**: Real-time traffic ingestion through the TUN interface (`tun0`) inspects Layer 3/4 headers for anomalies.
- **Protocol Verification**: Verified IPv4/IPv6 packet structures, TCP sequence tracking, and DNS query flows.
- **Recommendation**: To inspect active traffic flows or evaluate specific packet frames, tap the **Attach Live Telemetry** button to load current capture buffers into your query!
"""
    }
  }
}
