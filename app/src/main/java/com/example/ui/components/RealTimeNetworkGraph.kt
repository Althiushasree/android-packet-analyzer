package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.intelligence.CommunicationFlow
import com.example.data.intelligence.ObservedNetworkDevice
import com.example.data.intelligence.RealNetworkInterfaceInfo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RealTimeNetworkGraph(
  networkInfo: RealNetworkInterfaceInfo,
  devices: List<ObservedNetworkDevice>,
  flows: List<CommunicationFlow>,
  modifier: Modifier = Modifier
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val surfaceColor = MaterialTheme.colorScheme.surface
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
  val errorColor = MaterialTheme.colorScheme.error

  val infiniteTransition = rememberInfiniteTransition(label = "graph_pulse")
  val pulseProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .height(280.dp)
      .testTag("real_time_network_graph_canvas"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Center Node: Local Android Device
        val centerRadius = 24.dp.toPx()
        val orbitRadius = minOf(size.width, size.height) * 0.38f

        // Draw radial orbit rings
        drawCircle(
          color = primaryColor.copy(alpha = 0.15f),
          radius = orbitRadius,
          center = Offset(centerX, centerY),
          style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
        )

        // Draw pulse wave from center
        val waveRadius = centerRadius + (orbitRadius - centerRadius) * pulseProgress
        drawCircle(
          color = primaryColor.copy(alpha = (1f - pulseProgress) * 0.35f),
          radius = waveRadius,
          center = Offset(centerX, centerY),
          style = Stroke(width = 2.dp.toPx())
        )

        // Peripheral Nodes (Gateway + Observable Subnet Devices + Endpoints)
        val peripheralNodes = mutableListOf<Triple<String, String, Color>>()
        if (networkInfo.defaultGateway != "Not observable") {
          peripheralNodes.add(Triple(networkInfo.defaultGateway, "Gateway Router", tertiaryColor))
        }

        devices.filter { !it.isLocalHost && !it.isGateway }.take(5).forEach { dev ->
          val label = dev.vendor.split(" ").firstOrNull() ?: dev.ipAddress
          peripheralNodes.add(Triple(dev.ipAddress, label, secondaryColor))
        }

        flows.take(3).forEach { flow ->
          if (peripheralNodes.none { it.first == flow.destinationAddress }) {
            peripheralNodes.add(Triple(flow.destinationAddress, "Remote: ${flow.port}", if (flow.port == 80) errorColor else primaryColor))
          }
        }

        val totalNodes = maxOf(1, peripheralNodes.size)
        val angleStep = (2 * Math.PI) / totalNodes

        peripheralNodes.forEachIndexed { index, (ip, label, nodeColor) ->
          val angle = index * angleStep - Math.PI / 2
          val nodeX = (centerX + orbitRadius * cos(angle)).toFloat()
          val nodeY = (centerY + orbitRadius * sin(angle)).toFloat()

          // Draw edge from center to node
          drawLine(
            color = nodeColor.copy(alpha = 0.5f),
            start = Offset(centerX, centerY),
            end = Offset(nodeX, nodeY),
            strokeWidth = 2.dp.toPx()
          )

          // Draw active pulse packet along the edge
          val packetX = centerX + (nodeX - centerX) * ((pulseProgress + (index * 0.2f)) % 1f)
          val packetY = centerY + (nodeY - centerY) * ((pulseProgress + (index * 0.2f)) % 1f)
          drawCircle(
            color = nodeColor,
            radius = 3.5.dp.toPx(),
            center = Offset(packetX, packetY)
          )

          // Draw node body
          drawCircle(
            color = nodeColor,
            radius = 12.dp.toPx(),
            center = Offset(nodeX, nodeY)
          )
          drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(nodeX, nodeY)
          )

          // Draw Node Label text
          val paint = android.graphics.Paint().apply {
            color = onSurfaceColor.hashCode()
            textSize = 24f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
          }

          val labelY = if (nodeY > centerY) nodeY + 20.dp.toPx() else nodeY - 14.dp.toPx()
          drawContext.canvas.nativeCanvas.drawText(
            if (label.length > 12) label.substring(0, 10) + ".." else label,
            nodeX,
            labelY,
            paint
          )
        }

        // Draw Center Node
        drawCircle(
          color = primaryColor,
          radius = centerRadius,
          center = Offset(centerX, centerY)
        )
        drawCircle(
          color = Color.White,
          radius = 8.dp.toPx(),
          center = Offset(centerX, centerY)
        )

        val centerPaint = android.graphics.Paint().apply {
          color = primaryColor.hashCode()
          textSize = 28f
          isAntiAlias = true
          textAlign = android.graphics.Paint.Align.CENTER
          typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        drawContext.canvas.nativeCanvas.drawText(
          "LOCAL HOST",
          centerX,
          centerY + centerRadius + 16.dp.toPx(),
          centerPaint
        )
      }
    }
  }
}
