package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import com.example.ui.components.ChartPalette
import com.example.ui.components.DonutSegment
import com.example.ui.components.InteractiveDonutChart
import com.example.ui.components.OtherItemDetail
import com.example.ui.components.UsageTimelineChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Packet Capture Pro", appName)
  }

  @Test
  fun `usage timeline chart handles empty state gracefully without crash`() {
    var selectedScope by mutableStateOf(TimelineScope.DAILY)
    composeTestRule.setContent {
      UsageTimelineChart(
        scope = selectedScope,
        dataPoints = emptyList(),
        onScopeChanged = { selectedScope = it }
      )
    }

    composeTestRule.onNodeWithTag("usage_timeline_chart").assertExists()
    composeTestRule.onNodeWithTag("timeline_empty_state").assertExists()
  }

  @Test
  fun `usage timeline chart renders data points and responds to tab switches`() {
    var selectedScope by mutableStateOf(TimelineScope.DAILY)
    val mockPoints = listOf(
      TimelineDataPoint(label = "10:00", timestamp = 1000L, totalBytes = 5000000L, downloadBytes = 3500000L, uploadBytes = 1500000L, packetCount = 2100),
      TimelineDataPoint(label = "11:00", timestamp = 2000L, totalBytes = 12000000L, downloadBytes = 8000000L, uploadBytes = 4000000L, packetCount = 5400)
    )

    composeTestRule.setContent {
      UsageTimelineChart(
        scope = selectedScope,
        dataPoints = mockPoints,
        onScopeChanged = { selectedScope = it }
      )
    }

    composeTestRule.onNodeWithTag("usage_timeline_chart").assertExists()
    composeTestRule.onNodeWithTag("timeline_bars_container").assertExists()
    composeTestRule.onNodeWithTag("timeline_point_inspector").assertExists()

    // Test switching scope tab
    composeTestRule.onNodeWithTag("timeline_scope_tab_monthly").performScrollTo().performClick()
    assertEquals(TimelineScope.MONTHLY, selectedScope)
  }

  @Test
  fun `interactive donut chart renders top apps and other legend with 2-way selection`() {
    var selectedId by mutableStateOf<String?>(null)
    val segments = listOf(
      DonutSegment(
        id = "com.google.android.youtube",
        label = "YouTube",
        value = 35200L,
        downloadBytes = 30000L,
        uploadBytes = 5200L,
        packetCount = 420,
        percentage = 35.7f,
        color = ChartPalette[0]
      ),
      DonutSegment(
        id = "com.android.chrome",
        label = "Chrome",
        value = 24600L,
        downloadBytes = 18000L,
        uploadBytes = 6600L,
        packetCount = 310,
        percentage = 24.9f,
        color = ChartPalette[1]
      ),
      DonutSegment(
        id = "__other_apps__",
        label = "Other",
        value = 3400L,
        downloadBytes = 2000L,
        uploadBytes = 1400L,
        packetCount = 50,
        percentage = 3.4f,
        color = ChartPalette[5],
        isOther = true,
        otherItems = listOf(
          OtherItemDetail(id = "com.microsoft.teams", label = "Teams", value = 1200L, percentage = 1.2f),
          OtherItemDetail(id = "com.slack", label = "Slack", value = 900L, percentage = 0.9f)
        )
      )
    )

    composeTestRule.setContent {
      Box(
        modifier = Modifier.verticalScroll(rememberScrollState())
      ) {
        InteractiveDonutChart(
          segments = segments,
          centerTitle = "TOTAL APP DATA",
          centerValueFormatted = "98.7 KB",
          selectedSegmentId = selectedId,
          onSelectSegment = { selectedId = it.id }
        )
      }
    }

    composeTestRule.onNodeWithTag("interactive_donut_card").assertExists()
    composeTestRule.onNodeWithTag("donut_legend_container").assertExists()
    composeTestRule.onNodeWithTag("donut_legend_item_com.google.android.youtube").assertExists()
    composeTestRule.onNodeWithTag("donut_legend_item_com.android.chrome").assertExists()
    composeTestRule.onNodeWithTag("donut_legend_item___other_apps__").assertExists()

    // Test clicking YouTube item
    composeTestRule.onNodeWithTag("donut_legend_item_com.google.android.youtube")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    assertEquals("com.google.android.youtube", selectedId)

    // Test clicking Other item opens dialog
    composeTestRule.onNodeWithTag("donut_legend_item___other_apps__")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    assertEquals("__other_apps__", selectedId)
    composeTestRule.onNodeWithTag("other_traffic_dialog").assertExists()
  }

  @Test
  fun `capture screen displays demo banner and capture controls`() {
    val dummyStats = com.example.data.model.NetworkStats(
      totalPacketsCaptured = 120,
      totalBytesCaptured = 1048576L,
      downloadSpeedMbps = 12.4,
      uploadSpeedMbps = 4.2,
      durationSeconds = 65L
    )

    composeTestRule.setContent {
      com.example.ui.screens.CaptureScreen(
        isCapturing = true,
        isPaused = false,
        stats = dummyStats,
        activeInterface = "wlan0 (Wi-Fi)",
        promiscuousMode = true,
        captureFilter = "tcp.port == 443",
        isCaptureFilterValid = true,
        fileFormat = "PCAP",
        ringBufferSizeMb = 100,
        snapLength = 65535,
        recentPackets = emptyList(),
        onToggleCapture = {},
        onPauseResume = {},
        onClearPackets = {},
        onExportPcap = {},
        onSelectInterface = {},
        onTogglePromiscuous = {},
        onChangeFilter = {},
        onSelectFileFormat = {},
        onSelectRingBuffer = {},
        onSelectSnapLength = {},
        onPacketClick = {}
      )
    }

    composeTestRule.onNodeWithTag("capture_screen").assertExists()
    composeTestRule.onNodeWithTag("demo_mode_banner").assertExists()
    composeTestRule.onNodeWithTag("capture_status_card").assertExists()
    composeTestRule.onNodeWithTag("capture_start_stop_button").assertExists()
  }

  @Test
  fun `csv export utilities generate valid RFC 4180 csv text`() {
    val packets = listOf(
      com.example.data.model.PacketEntity(
        id = 1L,
        sessionId = "sess_1",
        timeFormatted = "12:00:01",
        appName = "Chrome",
        appPackage = "com.android.chrome",
        sourceIp = "192.168.1.5",
        sourcePort = 54321,
        destIp = "142.250.190.46",
        destPort = 443,
        host = "google.com",
        protocol = "TLS",
        length = 1400,
        info = "Application Data",
        payloadHex = "00 01",
        payloadAscii = ".."
      )
    )

    val csvOutput = com.example.util.CsvExportUtils.exportPacketsToCsv(packets)
    assertNotNull(csvOutput)
    org.junit.Assert.assertTrue(csvOutput.contains("No,Time,Source IP,Source Port,Destination IP,Dest Port,Host,Protocol,Length,Encrypted,Application,Info"))
    org.junit.Assert.assertTrue(csvOutput.contains("Chrome"))
    org.junit.Assert.assertTrue(csvOutput.contains("142.250.190.46"))
  }

  @Test
  fun `summary report generator creates complete text-based summary of captured network packets`() {
    val stats = com.example.data.model.NetworkStats(
      totalPacketsCaptured = 1500,
      totalBytesCaptured = 5242880L,
      downloadSpeedMbps = 15.2,
      uploadSpeedMbps = 6.4,
      durationSeconds = 120,
      activeConnectionsCount = 8,
      openSocketsCount = 14,
      totalAlarmsCount = 1
    )
    val app = com.example.data.model.DetailedAppTraffic(
      appName = "YouTube",
      appPackage = "com.google.android.youtube",
      totalBytes = 3145728L,
      downloadBytes = 2500000L,
      uploadBytes = 645728L,
      packetCount = 850,
      percentage = 60.0f
    )
    val ip = com.example.data.model.DetailedIpTraffic(
      ip = "142.250.190.46",
      hostname = "google.com",
      totalBytes = 3145728L,
      downloadBytes = 2500000L,
      uploadBytes = 645728L,
      packetCount = 850,
      percentage = 60.0f
    )

    val report = com.example.util.SummaryReportUtils.buildPacketSummaryReport(
      stats = stats,
      isCapturing = true,
      activeInterface = "wlan0 (Wi-Fi)",
      topApps = listOf(app),
      topIps = listOf(ip)
    )

    assertNotNull(report)
    org.junit.Assert.assertTrue(report.contains("PACKET CAPTURE PRO - NETWORK SUMMARY REPORT"))
    org.junit.Assert.assertTrue(report.contains("1. TRAFFIC & BANDWIDTH OVERVIEW"))
    org.junit.Assert.assertTrue(report.contains("2. TOP APPLICATIONS BREAKDOWN"))
    org.junit.Assert.assertTrue(report.contains("YouTube"))
    org.junit.Assert.assertTrue(report.contains("142.250.190.46"))
    org.junit.Assert.assertTrue(report.contains("Total Packets Captured : 1,500"))
  }

  @Test
  fun `expandable packet list reveals detailed metadata on row tap`() {
    val packet = com.example.data.model.PacketEntity(
      id = 101L,
      sessionId = "session_1",
      timestamp = 1723700000000L,
      timeFormatted = "10:45:22.150",
      appName = "Chrome",
      appPackage = "com.android.chrome",
      sourceIp = "192.168.1.50",
      sourcePort = 54321,
      destIp = "142.250.190.46",
      destPort = 443,
      host = "google.com",
      protocol = "TLS",
      length = 1420,
      info = "TLSv1.3 Application Data",
      isEncrypted = true,
      payloadHex = "17 03 03 00 20 01 02",
      payloadAscii = "TLS_PAYLOAD"
    )

    var inspectedPacket: com.example.data.model.PacketEntity? = null

    composeTestRule.setContent {
      com.example.ui.components.ExpandablePacketRowItem(
        packet = packet,
        isExpanded = true,
        onToggleExpand = {},
        onInspect = { inspectedPacket = packet }
      )
    }

    // Verify row item exists
    composeTestRule.onNodeWithTag("packet_item_101").assertExists()

    // Verify expanded details section is rendered with metadata (unmerged semantic tree)
    composeTestRule.onNodeWithTag("packet_expanded_details_101", useUnmergedTree = true).assertExists()

    // Test tapping inspect button inside the expanded details
    composeTestRule.onNodeWithTag("inspect_packet_button_101", useUnmergedTree = true).assertExists().performClick()
    composeTestRule.waitForIdle()
    org.junit.Assert.assertNotNull(inspectedPacket)
    org.junit.Assert.assertEquals(101L, inspectedPacket?.id)
  }
}
