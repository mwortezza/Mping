package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnState
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun LiveTelemetryWidget(
    vpnState: VpnState,
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    MpingLiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(26.dp),
        blurRadius = 25f,
        surfaceAlpha = 0.10f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Live Speed Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LiquidCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Download Speed",
                            tint = LiquidCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DOWNLOAD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatSpeed(vpnState.currentDownloadBps),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Upload Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NeonRose.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Upload Speed",
                            tint = NeonRose,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "UPLOAD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatSpeed(vpnState.currentUploadBps),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonRose,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Smooth Dual Bezier Speed Graph Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                val w = size.width
                val h = size.height

                // Draw subtle horizontal grid lines
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(0f, h * 0.5f),
                    end = Offset(w, h * 0.5f),
                    strokeWidth = 1f
                )

                // 1. Download Bezier Curve (Liquid Cyan)
                drawBezierGraph(
                    data = downloadHistory,
                    width = w,
                    height = h,
                    strokeColor = LiquidCyan,
                    areaTopColor = LiquidCyan.copy(alpha = 0.25f),
                    areaBottomColor = Color.Transparent
                )

                // 2. Upload Bezier Curve (Neon Rose)
                drawBezierGraph(
                    data = uploadHistory,
                    width = w,
                    height = h,
                    strokeColor = NeonRose,
                    areaTopColor = NeonRose.copy(alpha = 0.20f),
                    areaBottomColor = Color.Transparent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Session Stats Data Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "DATA IN", value = formatBytes(vpnState.totalRxBytes))
                StatItem(label = "DATA OUT", value = formatBytes(vpnState.totalTxBytes))
                StatItem(
                    label = "PROTOCOL",
                    value = vpnState.activeServer?.protocol?.displayName ?: "VLESS Reality"
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextHigh,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBezierGraph(
    data: List<Float>,
    width: Float,
    height: Float,
    strokeColor: Color,
    areaTopColor: Color,
    areaBottomColor: Color
) {
    if (data.size < 2) return

    val maxVal = (data.maxOrNull() ?: 1f).coerceAtLeast(10f)
    val points = data.mapIndexed { index, value ->
        val x = (index.toFloat() / (data.size - 1)) * width
        val normalized = (value / maxVal).coerceIn(0.05f, 0.95f)
        val y = height - (normalized * (height - 8f))
        Offset(x, y)
    }

    val strokePath = Path()
    val fillPath = Path()

    strokePath.moveTo(points.first().x, points.first().y)
    fillPath.moveTo(points.first().x, height)
    fillPath.lineTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val cx = (p0.x + p1.x) / 2f
        strokePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }

    fillPath.lineTo(points.last().x, height)
    fillPath.close()

    // Draw gradient fill area
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(areaTopColor, areaBottomColor),
            startY = 0f,
            endY = height
        )
    )

    // Draw stroke line
    drawPath(
        path = strokePath,
        color = strokeColor,
        style = Stroke(width = 2.5f)
    )
}

private fun formatSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
        bytesPerSec >= 1024 -> String.format("%d KB/s", bytesPerSec / 1024)
        else -> "$bytesPerSec B/s"
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L * 1024L -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format("%d KB", bytes / 1024)
        else -> "$bytes B"
    }
}
