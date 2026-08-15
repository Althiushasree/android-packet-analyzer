package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.TimelineDataPoint
import com.example.data.model.TimelineScope
import com.example.ui.components.UsageTimelineChart
import com.example.ui.theme.PacketCaptureTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun timeline_chart_screenshot() {
    val mockPoints = listOf(
      TimelineDataPoint(label = "10:00", timestamp = 1000L, totalBytes = 5000000L, downloadBytes = 3500000L, uploadBytes = 1500000L, packetCount = 2100),
      TimelineDataPoint(label = "11:00", timestamp = 2000L, totalBytes = 12000000L, downloadBytes = 8000000L, uploadBytes = 4000000L, packetCount = 5400)
    )
    composeTestRule.setContent {
      PacketCaptureTheme {
        UsageTimelineChart(
          scope = TimelineScope.DAILY,
          dataPoints = mockPoints,
          onScopeChanged = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timeline_chart.png")
  }
}
