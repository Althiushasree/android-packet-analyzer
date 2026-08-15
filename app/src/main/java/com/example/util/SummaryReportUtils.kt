package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.HighestTrafficConsumer
import com.example.data.model.NetworkAlarm
import com.example.data.model.NetworkStats
import com.example.data.model.PacketEntity
import com.example.data.model.ProtocolDistribution
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SummaryReportUtils {

  fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
      gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
      mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
      kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
      else -> "$bytes B"
    }
  }

  fun buildPacketSummaryReport(
    stats: NetworkStats,
    isCapturing: Boolean,
    activeInterface: String = "wlan0 (Wi-Fi)",
    topApps: List<DetailedAppTraffic> = emptyList(),
    topIps: List<DetailedIpTraffic> = emptyList(),
    protocols: List<ProtocolDistribution> = emptyList(),
    alarms: List<NetworkAlarm> = emptyList(),
    highestConsumer: HighestTrafficConsumer? = null,
    recentPackets: List<PacketEntity> = emptyList()
  ): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    val totalBytes = stats.totalBytesCaptured

    val sb = StringBuilder()
    sb.appendLine("================================================================================")
    sb.appendLine("                 PACKET CAPTURE PRO - NETWORK SUMMARY REPORT                    ")
    sb.appendLine("================================================================================")
    sb.appendLine("Generated At       : $dateStr")
    sb.appendLine("Capture Status     : ${if (isCapturing) "ACTIVE (Capturing Live Traffic)" else "PAUSED / IDLE"}")
    sb.appendLine("Network Interface  : $activeInterface")
    sb.appendLine("Capture Duration   : ${stats.durationSeconds}s")
    sb.appendLine("--------------------------------------------------------------------------------")
    sb.appendLine("1. TRAFFIC & BANDWIDTH OVERVIEW")
    sb.appendLine("--------------------------------------------------------------------------------")
    sb.appendLine("  • Total Packets Captured : ${String.format(Locale.US, "%,d", stats.totalPacketsCaptured)}")
    sb.appendLine("  • Total Traffic Volume   : ${formatBytes(totalBytes)}")
    sb.appendLine("  • Download Speed         : ${String.format(Locale.US, "%.2f", stats.downloadSpeedMbps)} MB/s")
    sb.appendLine("  • Upload Speed           : ${String.format(Locale.US, "%.2f", stats.uploadSpeedMbps)} MB/s")
    sb.appendLine("  • Active Connections     : ${stats.activeConnectionsCount}")
    sb.appendLine("  • Open Sockets           : ${stats.openSocketsCount}")
    if (highestConsumer != null && highestConsumer.topAppName.isNotEmpty()) {
      sb.appendLine("  • Top Application        : ${highestConsumer.topAppName} (${formatBytes(highestConsumer.topAppBytes)})")
      sb.appendLine("  • Top Destination IP     : ${highestConsumer.topIp} [${highestConsumer.topIpHostname}] (${formatBytes(highestConsumer.topIpBytes)})")
      sb.appendLine("  • Top Protocol           : ${highestConsumer.topProtocol} (${formatBytes(highestConsumer.topProtocolBytes)})")
    }
    sb.appendLine()

    if (topApps.isNotEmpty()) {
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine("2. TOP APPLICATIONS BREAKDOWN")
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine(String.format(Locale.US, "%-4s %-20s %-12s %-8s %-10s %-12s", "#", "Application", "Total Data", "Share", "Packets", "Top Protocol"))
      sb.appendLine("--------------------------------------------------------------------------------")
      topApps.take(8).forEachIndexed { idx, app ->
        val name = if (app.appName.length > 18) app.appName.take(16) + ".." else app.appName
        sb.appendLine(
          String.format(
            Locale.US,
            "%-4d %-20s %-12s %-8s %-10s %-12s",
            idx + 1,
            name,
            formatBytes(app.totalBytes),
            String.format(Locale.US, "%.1f%%", app.percentage),
            String.format(Locale.US, "%,d", app.packetCount),
            app.topProtocol
          )
        )
      }
      sb.appendLine()
    }

    if (topIps.isNotEmpty()) {
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine("3. TOP IP ADDRESSES & HOSTS")
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine(String.format(Locale.US, "%-4s %-18s %-20s %-12s %-8s %-10s", "#", "IP Address", "Hostname", "Total Data", "Share", "Packets"))
      sb.appendLine("--------------------------------------------------------------------------------")
      topIps.take(8).forEachIndexed { idx, ip ->
        val host = if (ip.hostname.length > 18) ip.hostname.take(16) + ".." else ip.hostname
        sb.appendLine(
          String.format(
            Locale.US,
            "%-4d %-18s %-20s %-12s %-8s %-10s",
            idx + 1,
            ip.ip,
            host,
            formatBytes(ip.totalBytes),
            String.format(Locale.US, "%.1f%%", ip.percentage),
            String.format(Locale.US, "%,d", ip.packetCount)
          )
        )
      }
      sb.appendLine()
    }

    if (protocols.isNotEmpty()) {
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine("4. PROTOCOL DISTRIBUTION")
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine(String.format(Locale.US, "%-12s %-12s %-14s %-8s", "Protocol", "Packets", "Volume", "Share"))
      sb.appendLine("--------------------------------------------------------------------------------")
      protocols.forEach { p ->
        sb.appendLine(
          String.format(
            Locale.US,
            "%-12s %-12s %-14s %-8s",
            p.protocol,
            String.format(Locale.US, "%,d", p.count),
            formatBytes(p.bytes),
            String.format(Locale.US, "%.1f%%", p.percentage)
          )
        )
      }
      sb.appendLine()
    }

    if (alarms.isNotEmpty()) {
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine("5. SECURITY ALARMS & INCIDENTS (${alarms.size} total)")
      sb.appendLine("--------------------------------------------------------------------------------")
      alarms.take(5).forEachIndexed { idx, alarm ->
        sb.appendLine("  [${alarm.severity.name}] ${alarm.title} (${alarm.timeFormatted})")
        sb.appendLine("    Details: ${alarm.message}")
      }
      sb.appendLine()
    }

    if (recentPackets.isNotEmpty()) {
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine("6. RECENT PACKET SAMPLES (Last ${recentPackets.size} packets)")
      sb.appendLine("--------------------------------------------------------------------------------")
      sb.appendLine(String.format(Locale.US, "%-4s %-12s %-20s %-20s %-6s %-6s %-12s", "#", "Time", "Source", "Destination", "Proto", "Len", "App"))
      sb.appendLine("--------------------------------------------------------------------------------")
      recentPackets.forEachIndexed { idx, pkt ->
        val src = "${pkt.sourceIp}:${pkt.sourcePort}"
        val dst = "${pkt.destIp}:${pkt.destPort}"
        val srcCut = if (src.length > 19) src.take(17) + ".." else src
        val dstCut = if (dst.length > 19) dst.take(17) + ".." else dst
        sb.appendLine(
          String.format(
            Locale.US,
            "%-4d %-12s %-20s %-20s %-6s %-6d %-12s",
            idx + 1,
            pkt.timeFormatted,
            srcCut,
            dstCut,
            pkt.protocol,
            pkt.length,
            pkt.appName
          )
        )
      }
      sb.appendLine()
    }

    sb.appendLine("================================================================================")
    sb.appendLine("End of Report | Generated by Packet Capture Pro Android")
    sb.appendLine("================================================================================")
    return sb.toString()
  }

  fun shareTextReport(context: Context, reportContent: String, title: String = "Packet Capture Pro - Network Summary Report") {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, title)
      putExtra(Intent.EXTRA_TEXT, reportContent)
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(shareIntent)
  }
}
