package com.example.data.vpn

import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Parsed representation of an IP packet captured from the TUN interface.
 */
data class ParsedIpPacket(
  val timestamp: Long = System.currentTimeMillis(),
  val timeFormatted: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
  val version: Int,
  val protocol: String,
  val sourceIp: String,
  val sourcePort: Int,
  val destIp: String,
  val destPort: Int,
  val host: String,
  val length: Int,
  val info: String,
  val status: String = "ACTIVE",
  val isEncrypted: Boolean = false,
  val isDecryptedHttp: Boolean = false,
  val httpMethod: String? = null,
  val httpUrl: String? = null,
  val httpStatusCode: Int? = null,
  val tlsSni: String? = null,
  val tlsCipherSuite: String? = null,
  val payloadHex: String = "",
  val payloadAscii: String = ""
)

/**
 * High-performance, robust IP and Transport layer packet decoder.
 */
object RawPacketParser {

  fun parse(buffer: ByteArray, bytesRead: Int): ParsedIpPacket? {
    if (bytesRead < 20) return null

    val version = (buffer[0].toInt() shr 4) and 0x0F
    return when (version) {
      4 -> parseIpv4(buffer, bytesRead)
      6 -> parseIpv6(buffer, bytesRead)
      else -> null
    }
  }

  private fun parseIpv4(buffer: ByteArray, bytesRead: Int): ParsedIpPacket? {
    if (bytesRead < 20) return null

    val ihl = (buffer[0].toInt() and 0x0F) * 4
    if (ihl < 20 || ihl > bytesRead) return null

    val totalLength = ((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)
    val actualPacketLength = minOf(bytesRead, if (totalLength in 20..bytesRead) totalLength else bytesRead)

    val protocolNum = buffer[9].toInt() and 0xFF

    val srcIpBytes = ByteArray(4)
    val dstIpBytes = ByteArray(4)
    System.arraycopy(buffer, 12, srcIpBytes, 0, 4)
    System.arraycopy(buffer, 16, dstIpBytes, 0, 4)

    val sourceIp = InetAddress.getByAddress(srcIpBytes).hostAddress ?: "0.0.0.0"
    val destIp = InetAddress.getByAddress(dstIpBytes).hostAddress ?: "0.0.0.0"

    return when (protocolNum) {
      6 -> parseTcp(buffer, ihl, actualPacketLength, sourceIp, destIp, 4)
      17 -> parseUdp(buffer, ihl, actualPacketLength, sourceIp, destIp, 4)
      1 -> parseIcmp(buffer, ihl, actualPacketLength, sourceIp, destIp, 4)
      else -> {
        val payloadBytes = actualPacketLength - ihl
        val hex = formatHexDump(buffer, ihl, minOf(payloadBytes, 64))
        val ascii = formatAsciiDump(buffer, ihl, minOf(payloadBytes, 64))
        ParsedIpPacket(
          version = 4,
          protocol = "IP($protocolNum)",
          sourceIp = sourceIp,
          sourcePort = 0,
          destIp = destIp,
          destPort = 0,
          host = destIp,
          length = actualPacketLength,
          info = "Raw IP protocol $protocolNum, len=$actualPacketLength",
          payloadHex = hex,
          payloadAscii = ascii
        )
      }
    }
  }

  private fun parseIpv6(buffer: ByteArray, bytesRead: Int): ParsedIpPacket? {
    if (bytesRead < 40) return null

    val payloadLength = ((buffer[4].toInt() and 0xFF) shl 8) or (buffer[5].toInt() and 0xFF)
    val nextHeader = buffer[6].toInt() and 0xFF

    val srcIpBytes = ByteArray(16)
    val dstIpBytes = ByteArray(16)
    System.arraycopy(buffer, 8, srcIpBytes, 0, 16)
    System.arraycopy(buffer, 24, dstIpBytes, 0, 16)

    val sourceIp = InetAddress.getByAddress(srcIpBytes).hostAddress ?: "::"
    val destIp = InetAddress.getByAddress(dstIpBytes).hostAddress ?: "::"

    val actualLen = minOf(bytesRead, 40 + payloadLength)
    return when (nextHeader) {
      6 -> parseTcp(buffer, 40, actualLen, sourceIp, destIp, 6)
      17 -> parseUdp(buffer, 40, actualLen, sourceIp, destIp, 6)
      58 -> parseIcmp(buffer, 40, actualLen, sourceIp, destIp, 6)
      else -> {
        ParsedIpPacket(
          version = 6,
          protocol = "IPv6($nextHeader)",
          sourceIp = sourceIp,
          sourcePort = 0,
          destIp = destIp,
          destPort = 0,
          host = destIp,
          length = actualLen,
          info = "IPv6 Next Header $nextHeader, len=$actualLen",
          payloadHex = formatHexDump(buffer, 40, minOf(actualLen - 40, 64)),
          payloadAscii = formatAsciiDump(buffer, 40, minOf(actualLen - 40, 64))
        )
      }
    }
  }

  private fun parseTcp(
    buffer: ByteArray,
    headerOffset: Int,
    totalLength: Int,
    sourceIp: String,
    destIp: String,
    ipVersion: Int
  ): ParsedIpPacket {
    if (headerOffset + 20 > totalLength) {
      return fallbackPacket(ipVersion, "TCP", sourceIp, 0, destIp, 0, totalLength, "Truncated TCP header")
    }

    val srcPort = ((buffer[headerOffset].toInt() and 0xFF) shl 8) or (buffer[headerOffset + 1].toInt() and 0xFF)
    val dstPort = ((buffer[headerOffset + 2].toInt() and 0xFF) shl 8) or (buffer[headerOffset + 3].toInt() and 0xFF)

    val seqNum = ((buffer[headerOffset + 4].toLong() and 0xFF) shl 24) or
        ((buffer[headerOffset + 5].toLong() and 0xFF) shl 16) or
        ((buffer[headerOffset + 6].toLong() and 0xFF) shl 8) or
        (buffer[headerOffset + 7].toLong() and 0xFF)

    val dataOffset = ((buffer[headerOffset + 12].toInt() shr 4) and 0x0F) * 4
    val flags = buffer[headerOffset + 13].toInt() and 0xFF

    val syn = (flags and 0x02) != 0
    val ack = (flags and 0x10) != 0
    val fin = (flags and 0x01) != 0
    val rst = (flags and 0x04) != 0
    val psh = (flags and 0x08) != 0

    val flagList = mutableListOf<String>()
    if (syn) flagList.add("SYN")
    if (ack) flagList.add("ACK")
    if (fin) flagList.add("FIN")
    if (rst) flagList.add("RST")
    if (psh) flagList.add("PSH")
    val flagStr = if (flagList.isNotEmpty()) flagList.joinToString(",") else "NONE"

    val payloadOffset = headerOffset + maxOf(20, dataOffset)
    val payloadLength = maxOf(0, totalLength - payloadOffset)

    var protocol = "TCP"
    var host = destIp
    var isEncrypted = false
    var isDecryptedHttp = false
    var httpMethod: String? = null
    var httpUrl: String? = null
    var httpStatusCode: Int? = null
    var tlsSni: String? = null
    var tlsCipherSuite: String? = null
    var info = "$srcPort → $dstPort [$flagStr] Seq=$seqNum Len=$payloadLength"

    if (payloadLength > 0 && payloadOffset + payloadLength <= totalLength) {
      // Check for TLS Handshake (0x16)
      if (dstPort == 443 || srcPort == 443 || buffer[payloadOffset] == 0x16.toByte()) {
        protocol = "TLS"
        isEncrypted = true
        tlsSni = extractTlsSni(buffer, payloadOffset, payloadLength)
        if (tlsSni != null) {
          host = tlsSni
          info = "TLS ClientHello SNI=$tlsSni"
        } else {
          info = if (dstPort == 443) "TLS Application Data / Session ($dstPort)" else "TLS Traffic ($srcPort → $dstPort)"
        }
      } else if (dstPort == 80 || srcPort == 80 || dstPort == 8080 || isHttpPayload(buffer, payloadOffset, payloadLength)) {
        protocol = "HTTP"
        val parsedHttp = parseHttpPayload(buffer, payloadOffset, payloadLength)
        if (parsedHttp != null) {
          httpMethod = parsedHttp.method
          httpUrl = parsedHttp.url
          httpStatusCode = parsedHttp.statusCode
          if (parsedHttp.host.isNotBlank()) host = parsedHttp.host
          info = parsedHttp.summary
          isDecryptedHttp = true
        }
      }
    }

    val hex = formatHexDump(buffer, payloadOffset, minOf(payloadLength, 128))
    val ascii = formatAsciiDump(buffer, payloadOffset, minOf(payloadLength, 128))

    return ParsedIpPacket(
      version = ipVersion,
      protocol = protocol,
      sourceIp = sourceIp,
      sourcePort = srcPort,
      destIp = destIp,
      destPort = dstPort,
      host = host,
      length = totalLength,
      info = info,
      status = if (rst) "CLOSED" else "ACTIVE",
      isEncrypted = isEncrypted,
      isDecryptedHttp = isDecryptedHttp,
      httpMethod = httpMethod,
      httpUrl = httpUrl,
      httpStatusCode = httpStatusCode,
      tlsSni = tlsSni,
      tlsCipherSuite = tlsCipherSuite,
      payloadHex = hex,
      payloadAscii = ascii
    )
  }

  private fun parseUdp(
    buffer: ByteArray,
    headerOffset: Int,
    totalLength: Int,
    sourceIp: String,
    destIp: String,
    ipVersion: Int
  ): ParsedIpPacket {
    if (headerOffset + 8 > totalLength) {
      return fallbackPacket(ipVersion, "UDP", sourceIp, 0, destIp, 0, totalLength, "Truncated UDP header")
    }

    val srcPort = ((buffer[headerOffset].toInt() and 0xFF) shl 8) or (buffer[headerOffset + 1].toInt() and 0xFF)
    val dstPort = ((buffer[headerOffset + 2].toInt() and 0xFF) shl 8) or (buffer[headerOffset + 3].toInt() and 0xFF)
    val udpLen = ((buffer[headerOffset + 4].toInt() and 0xFF) shl 8) or (buffer[headerOffset + 5].toInt() and 0xFF)

    val payloadOffset = headerOffset + 8
    val payloadLength = maxOf(0, minOf(udpLen - 8, totalLength - payloadOffset))

    var protocol = "UDP"
    var host = destIp
    var info = "$srcPort → $dstPort Len=$payloadLength"
    var isEncrypted = false

    if (dstPort == 53 || srcPort == 53) {
      protocol = "DNS"
      val dnsDomain = extractDnsQueryName(buffer, payloadOffset, payloadLength)
      if (dnsDomain.isNotBlank()) {
        host = dnsDomain
        info = if (dstPort == 53) "Standard query A $dnsDomain" else "Standard query response $dnsDomain"
      } else {
        info = "DNS Query/Response transaction"
      }
    } else if (dstPort == 443 || srcPort == 443) {
      protocol = "QUIC"
      isEncrypted = true
      info = "QUIC / HTTP3 Handshake & Flow ($srcPort → $dstPort)"
    } else if (dstPort == 67 || dstPort == 68) {
      protocol = "DHCP"
      info = "DHCP BootP packet"
    } else if (dstPort == 123 || srcPort == 123) {
      protocol = "NTP"
      info = "Network Time Protocol"
    }

    val hex = formatHexDump(buffer, payloadOffset, minOf(payloadLength, 128))
    val ascii = formatAsciiDump(buffer, payloadOffset, minOf(payloadLength, 128))

    return ParsedIpPacket(
      version = ipVersion,
      protocol = protocol,
      sourceIp = sourceIp,
      sourcePort = srcPort,
      destIp = destIp,
      destPort = dstPort,
      host = host,
      length = totalLength,
      info = info,
      isEncrypted = isEncrypted,
      payloadHex = hex,
      payloadAscii = ascii
    )
  }

  private fun parseIcmp(
    buffer: ByteArray,
    headerOffset: Int,
    totalLength: Int,
    sourceIp: String,
    destIp: String,
    ipVersion: Int
  ): ParsedIpPacket {
    val type = if (headerOffset < totalLength) buffer[headerOffset].toInt() and 0xFF else 0
    val code = if (headerOffset + 1 < totalLength) buffer[headerOffset + 1].toInt() and 0xFF else 0

    val info = when (type) {
      8 -> "Echo (ping) request id=0x01 seq=1"
      0 -> "Echo (ping) reply id=0x01 seq=1"
      3 -> "Destination unreachable (Code $code)"
      11 -> "Time-to-live exceeded"
      else -> "ICMP Type $type, Code $code"
    }

    return ParsedIpPacket(
      version = ipVersion,
      protocol = "ICMP",
      sourceIp = sourceIp,
      sourcePort = 0,
      destIp = destIp,
      destPort = 0,
      host = destIp,
      length = totalLength,
      info = info,
      payloadHex = formatHexDump(buffer, headerOffset, minOf(totalLength - headerOffset, 32)),
      payloadAscii = formatAsciiDump(buffer, headerOffset, minOf(totalLength - headerOffset, 32))
    )
  }

  private fun fallbackPacket(
    version: Int,
    protocol: String,
    srcIp: String,
    srcPort: Int,
    dstIp: String,
    dstPort: Int,
    length: Int,
    info: String
  ): ParsedIpPacket {
    return ParsedIpPacket(
      version = version,
      protocol = protocol,
      sourceIp = srcIp,
      sourcePort = srcPort,
      destIp = dstIp,
      destPort = dstPort,
      host = dstIp,
      length = length,
      info = info
    )
  }

  /**
   * Decodes DNS domain names from question record (e.g. \x06google\x03com\x00 -> google.com)
   */
  private fun extractDnsQueryName(buffer: ByteArray, offset: Int, length: Int): String {
    if (length < 12) return ""
    try {
      var pos = offset + 12 // DNS header length is 12 bytes
      val end = offset + length
      val sb = StringBuilder()

      while (pos < end) {
        val labelLen = buffer[pos].toInt() and 0xFF
        if (labelLen == 0) break
        if ((labelLen and 0xC0) == 0xC0) {
          // Compression pointer
          break
        }
        pos++
        if (pos + labelLen > end) break
        if (sb.isNotEmpty()) sb.append('.')
        sb.append(String(buffer, pos, labelLen, StandardCharsets.US_ASCII))
        pos += labelLen
      }
      return sb.toString()
    } catch (_: Exception) {
      return ""
    }
  }

  /**
   * Extracts Server Name Indication (SNI) from TLS ClientHello packet.
   */
  private fun extractTlsSni(buffer: ByteArray, offset: Int, length: Int): String? {
    try {
      if (length < 44) return null
      var pos = offset

      // Check Content Type = Handshake (0x16)
      if (buffer[pos] != 0x16.toByte()) return null
      pos += 5 // Skip TLS Record Header (Type: 1, Version: 2, Length: 2)

      if (pos >= offset + length) return null
      // Check Handshake Type = ClientHello (0x01)
      if (buffer[pos] != 0x01.toByte()) return null
      pos += 4 // Skip Handshake Type (1) + Handshake Length (3)

      pos += 2 + 32 // Skip Client Version (2) + Random (32)
      if (pos >= offset + length) return null

      // Session ID
      val sessionIdLen = buffer[pos].toInt() and 0xFF
      pos += 1 + sessionIdLen
      if (pos + 2 > offset + length) return null

      // Cipher Suites
      val cipherSuitesLen = ((buffer[pos].toInt() and 0xFF) shl 8) or (buffer[pos + 1].toInt() and 0xFF)
      pos += 2 + cipherSuitesLen
      if (pos + 1 > offset + length) return null

      // Compression Methods
      val compLen = buffer[pos].toInt() and 0xFF
      pos += 1 + compLen
      if (pos + 2 > offset + length) return null

      // Extensions Length
      val extensionsLen = ((buffer[pos].toInt() and 0xFF) shl 8) or (buffer[pos + 1].toInt() and 0xFF)
      pos += 2
      val extensionsEnd = pos + extensionsLen

      while (pos + 4 <= minOf(extensionsEnd, offset + length)) {
        val extType = ((buffer[pos].toInt() and 0xFF) shl 8) or (buffer[pos + 1].toInt() and 0xFF)
        val extLen = ((buffer[pos + 2].toInt() and 0xFF) shl 8) or (buffer[pos + 3].toInt() and 0xFF)
        pos += 4

        if (extType == 0x0000) { // Server Name Indication (SNI)
          if (pos + 5 <= offset + length) {
            val serverNameListLen = ((buffer[pos].toInt() and 0xFF) shl 8) or (buffer[pos + 1].toInt() and 0xFF)
            val nameType = buffer[pos + 2].toInt() and 0xFF
            val nameLen = ((buffer[pos + 3].toInt() and 0xFF) shl 8) or (buffer[pos + 4].toInt() and 0xFF)
            if (nameType == 0 && pos + 5 + nameLen <= offset + length) {
              return String(buffer, pos + 5, nameLen, StandardCharsets.US_ASCII)
            }
          }
        }
        pos += extLen
      }
    } catch (_: Exception) {
      return null
    }
    return null
  }

  private fun isHttpPayload(buffer: ByteArray, offset: Int, length: Int): Boolean {
    if (length < 4) return false
    val prefix = String(buffer, offset, minOf(length, 10), StandardCharsets.US_ASCII)
    return prefix.startsWith("GET ") ||
        prefix.startsWith("POST ") ||
        prefix.startsWith("HEAD ") ||
        prefix.startsWith("PUT ") ||
        prefix.startsWith("DELETE ") ||
        prefix.startsWith("HTTP/1.")
  }

  private data class HttpParsedInfo(
    val method: String?,
    val url: String?,
    val host: String,
    val statusCode: Int?,
    val summary: String
  )

  private fun parseHttpPayload(buffer: ByteArray, offset: Int, length: Int): HttpParsedInfo? {
    try {
      val text = String(buffer, offset, minOf(length, 512), StandardCharsets.ISO_8859_1)
      val lines = text.split("\r\n", "\n")
      if (lines.isEmpty()) return null

      val firstLine = lines[0]
      var method: String? = null
      var url: String? = null
      var statusCode: Int? = null
      var host = ""

      if (firstLine.startsWith("HTTP/1.")) {
        val parts = firstLine.split(" ")
        if (parts.size >= 2) {
          statusCode = parts[1].toIntOrNull()
        }
      } else {
        val parts = firstLine.split(" ")
        if (parts.size >= 2) {
          method = parts[0]
          url = parts[1]
        }
      }

      for (line in lines) {
        if (line.startsWith("Host:", ignoreCase = true)) {
          host = line.substring(5).trim()
          break
        }
      }

      return HttpParsedInfo(
        method = method,
        url = url,
        host = host,
        statusCode = statusCode,
        summary = firstLine.take(80)
      )
    } catch (_: Exception) {
      return null
    }
  }

  private fun formatHexDump(buffer: ByteArray, offset: Int, length: Int): String {
    if (length <= 0 || offset >= buffer.size) return ""
    val actualLen = minOf(length, buffer.size - offset)
    val sb = StringBuilder()
    for (i in 0 until actualLen) {
      sb.append(String.format(Locale.US, "%02x ", buffer[offset + i]))
      if ((i + 1) % 16 == 0 && i + 1 < actualLen) {
        sb.append("\n")
      }
    }
    return sb.toString().trim()
  }

  private fun formatAsciiDump(buffer: ByteArray, offset: Int, length: Int): String {
    if (length <= 0 || offset >= buffer.size) return ""
    val actualLen = minOf(length, buffer.size - offset)
    val sb = StringBuilder()
    for (i in 0 until actualLen) {
      val b = buffer[offset + i].toInt() and 0xFF
      if (b in 32..126) {
        sb.append(b.toChar())
      } else {
        sb.append('.')
      }
    }
    return sb.toString()
  }
}
