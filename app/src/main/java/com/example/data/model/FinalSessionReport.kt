package com.example.data.model

import java.util.Locale

/**
 * Represents a complete, finalized report generated when the user presses STOP on packet capture.
 * Computed from the full session dataset, distinct from transient live snapshots.
 */
data class FinalSessionReport(
  val sessionId: String,
  val startTime: Long,
  val endTime: Long,
  val durationSeconds: Long,
  val totalPackets: Long,
  val totalBytes: Long,
  val downloadBytes: Long,
  val uploadBytes: Long,
  val avgThroughputMbps: Double,
  val peakThroughputMbps: Double,
  val activeConnectionsCount: Int,
  val topApplications: List<DetailedAppTraffic> = emptyList(),
  val topIpAddresses: List<DetailedIpTraffic> = emptyList(),
  val protocolDistribution: List<ProtocolDistribution> = emptyList(),
  val highestConsumer: HighestTrafficConsumer = HighestTrafficConsumer(
    topAppName = "None",
    topAppBytes = 0L,
    topIp = "None",
    topIpHostname = "None",
    topIpBytes = 0L,
    topConnection = "None",
    topConnectionBytes = 0L,
    topProtocol = "None",
    topProtocolBytes = 0L
  ),
  val generatedTimestampFormatted: String = ""
) {
  val formattedDuration: String
    get() {
      val mins = durationSeconds / 60
      val secs = durationSeconds % 60
      return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

  val formattedTotalBytes: String
    get() {
      return if (totalBytes >= 1024 * 1024 * 1024) {
        String.format(Locale.US, "%.2f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
      } else if (totalBytes >= 1024 * 1024) {
        String.format(Locale.US, "%.2f MB", totalBytes / (1024.0 * 1024.0))
      } else if (totalBytes >= 1024) {
        String.format(Locale.US, "%.1f KB", totalBytes / 1024.0)
      } else {
        "$totalBytes B"
      }
    }
}
