package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.ConversationItem
import com.example.data.model.DetailedAppTraffic
import com.example.data.model.DetailedIpTraffic
import com.example.data.model.EndpointItem
import com.example.data.model.PacketEntity
import com.example.data.model.ProtocolDistribution
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TrafficAlertItem
import java.util.Locale

object CsvExportUtils {

  fun exportPacketsToCsv(packets: List<PacketEntity>): String {
    val sb = StringBuilder()
    sb.append("No,Time,Source IP,Source Port,Destination IP,Dest Port,Host,Protocol,Length,Encrypted,Application,Info\n")
    packets.forEachIndexed { index, p ->
      val escapedInfo = "\"${p.info.replace("\"", "\"\"")}\""
      val escapedHost = "\"${p.host.replace("\"", "\"\"")}\""
      val escapedApp = "\"${p.appName.replace("\"", "\"\"")}\""
      sb.append("${index + 1},${p.timeFormatted},${p.sourceIp},${p.sourcePort},${p.destIp},${p.destPort},$escapedHost,${p.protocol},${p.length},${p.isEncrypted},$escapedApp,$escapedInfo\n")
    }
    return sb.toString()
  }

  fun exportAppsToCsv(apps: List<DetailedAppTraffic>): String {
    val sb = StringBuilder()
    sb.append("Application Name,Package,Total Bytes,Upload Bytes,Download Bytes,Packets,Percentage,Avg Kbps,Peak Kbps,Connections,Top Protocol,Top Destination IP\n")
    apps.forEach { a ->
      val name = "\"${a.appName.replace("\"", "\"\"")}\""
      sb.append("$name,${a.appPackage},${a.totalBytes},${a.uploadBytes},${a.downloadBytes},${a.packetCount},${String.format(Locale.US, "%.2f", a.percentage)}%,${String.format(Locale.US, "%.1f", a.avgThroughputKbps)},${String.format(Locale.US, "%.1f", a.peakThroughputKbps)},${a.connectionCount},${a.topProtocol},${a.topDestIp}\n")
    }
    return sb.toString()
  }

  fun exportIpsToCsv(ips: List<DetailedIpTraffic>): String {
    val sb = StringBuilder()
    sb.append("IP Address,Hostname,Total Bytes,Upload Bytes,Download Bytes,Packets,Percentage,Country,Is Local,Protocols\n")
    ips.forEach { ip ->
      val host = "\"${ip.hostname.replace("\"", "\"\"")}\""
      val protos = "\"${ip.protocols.joinToString("; ")}\""
      sb.append("${ip.ip},$host,${ip.totalBytes},${ip.uploadBytes},${ip.downloadBytes},${ip.packetCount},${String.format(Locale.US, "%.2f", ip.percentage)}%,${ip.country},${ip.isLocal},$protos\n")
    }
    return sb.toString()
  }

  fun exportConversationsToCsv(conversations: List<ConversationItem>): String {
    val sb = StringBuilder()
    sb.append("Source IP,Source Port,Destination IP,Dest Port,Protocol,Packets,Total Bytes,Start Time,Duration (s),Application\n")
    conversations.forEach { c ->
      val app = "\"${c.appName.replace("\"", "\"\"")}\""
      sb.append("${c.sourceIp},${c.sourcePort},${c.destIp},${c.destPort},${c.protocol},${c.packetCount},${c.totalBytes},${c.startTimeFormatted},${String.format(Locale.US, "%.2f", c.durationSeconds)},$app\n")
    }
    return sb.toString()
  }

  fun exportEndpointsToCsv(endpoints: List<EndpointItem>): String {
    val sb = StringBuilder()
    sb.append("Address,Type,Hostname,Packets,Total Bytes,Sent Bytes,Received Bytes,Connections\n")
    endpoints.forEach { e ->
      val host = "\"${e.hostname.replace("\"", "\"\"")}\""
      sb.append("${e.address},${e.type},$host,${e.packetCount},${e.totalBytes},${e.sentBytes},${e.receivedBytes},${e.connectionCount}\n")
    }
    return sb.toString()
  }

  fun exportProtocolsToCsv(protocols: List<ProtocolDistribution>): String {
    val sb = StringBuilder()
    sb.append("Protocol,Packets,Total Bytes,Percentage\n")
    protocols.forEach { p ->
      sb.append("${p.protocol},${p.count},${p.bytes},${String.format(Locale.US, "%.2f", p.percentage)}%\n")
    }
    return sb.toString()
  }

  fun exportTimelineToCsv(points: List<TimelineDataPoint>): String {
    val sb = StringBuilder()
    sb.append("Time Period,Total Bytes,Upload Bytes,Download Bytes,Packet Count\n")
    points.forEach { t ->
      val label = "\"${t.label.replace("\"", "\"\"")}\""
      sb.append("$label,${t.totalBytes},${t.uploadBytes},${t.downloadBytes},${t.packetCount}\n")
    }
    return sb.toString()
  }

  fun exportAlertsToCsv(alerts: List<TrafficAlertItem>): String {
    val sb = StringBuilder()
    sb.append("Time,Severity,Category,Entity,Reason,Current Traffic,Threshold,Usage Percentage\n")
    alerts.forEach { a ->
      val reason = "\"${a.reason.replace("\"", "\"\"")}\""
      val entity = "\"${a.entityName.replace("\"", "\"\"")}\""
      sb.append("${a.timeFormatted},${a.severity},${a.category},$entity,$reason,${a.currentTrafficFormatted},${a.thresholdFormatted},${String.format(Locale.US, "%.1f", a.percentageOfThreshold)}%\n")
    }
    return sb.toString()
  }

  fun shareCsv(context: Context, csvContent: String, title: String) {
    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, csvContent)
      putExtra(Intent.EXTRA_TITLE, title)
      type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(shareIntent)
  }
}
