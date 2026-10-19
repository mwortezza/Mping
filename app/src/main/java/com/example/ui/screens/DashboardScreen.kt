package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.ServerProfile
import com.example.model.VpnState
import com.example.ui.components.LiveTelemetryWidget
import com.example.ui.components.MpingLiquidCore
import com.example.ui.components.MpingLiquidGlassCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.PingFair
import com.example.ui.theme.PingGood
import com.example.ui.theme.PingSlow
import com.example.ui.theme.PingUltraFast
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun DashboardScreen(
    vpnState: VpnState,
    activeServer: ServerProfile?,
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    onToggleVpn: () -> Unit,
    onOpenServerMatrix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Centerpiece: The Mping 3D Liquid Core Sphere
        MpingLiquidCore(
            vpnState = vpnState,
            onToggleConnection = onToggleVpn,
            modifier = Modifier.testTag("liquid_core_sphere")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Active Server Quick Selector Glass Card
        activeServer?.let { server ->
            MpingLiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("quick_server_selector")
                    .clickable { onOpenServerMatrix() },
                shape = RoundedCornerShape(22.dp),
                blurRadius = 25f,
                surfaceAlpha = 0.12f
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Country Flag & Protocol Icon
                        val flag = when (server.countryCode) {
                            "US" -> "🇺🇸"
                            "DE" -> "🇩🇪"
                            "JP" -> "🇯🇵"
                            "SG" -> "🇸🇬"
                            "GB" -> "🇬🇧"
                            "NL" -> "🇳🇱"
                            "CA" -> "🇨🇦"
                            else -> "🌐"
                        }

                        Text(
                            text = flag,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = server.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHigh,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElectricIndigo.copy(alpha = 0.22f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = server.protocol.displayName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiquidCyan
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${server.serverAddress}:${server.serverPort}",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Ping badge & Chevron
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pingColor = when {
                            server.lastPingMs in 1..79 -> PingUltraFast
                            server.lastPingMs in 80..149 -> PingGood
                            server.lastPingMs in 150..299 -> PingFair
                            server.lastPingMs >= 300 -> PingSlow
                            else -> TextMuted
                        }
                        Text(
                            text = if (server.lastPingMs > 0) "${server.lastPingMs} ms" else "-- ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = pingColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Switch Server",
                            tint = TextMedium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Smooth Bezier Curve Live Telemetry Widget
        LiveTelemetryWidget(
            vpnState = vpnState,
            downloadHistory = downloadHistory,
            uploadHistory = uploadHistory,
            modifier = Modifier.testTag("live_telemetry_widget")
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
