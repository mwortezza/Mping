package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.VpnState
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.PingFair
import com.example.ui.theme.PingGood
import com.example.ui.theme.PingSlow
import com.example.ui.theme.PingUltraFast
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun FloatingGlassHud(
    vpnState: VpnState,
    onTestPingAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isConnecting = vpnState.status == ConnectionStatus.CONNECTING
    val isConnected = vpnState.status == ConnectionStatus.CONNECTED

    val statusColor = when (vpnState.status) {
        ConnectionStatus.CONNECTED -> LiquidCyan
        ConnectionStatus.CONNECTING -> PingFair
        ConnectionStatus.DISCONNECTING -> NeonRose
        ConnectionStatus.DISCONNECTED -> TextMuted
    }

    val pingColor = when {
        vpnState.currentPingMs in 1..79 -> PingUltraFast
        vpnState.currentPingMs in 80..149 -> PingGood
        vpnState.currentPingMs in 150..299 -> PingFair
        vpnState.currentPingMs >= 300 -> PingSlow
        else -> TextMuted
    }

    MpingLiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        blurRadius = 30f,
        surfaceAlpha = 0.12f,
        hasChromaticGlow = isConnected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing Prism Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    LiquidCyan.copy(alpha = 0.25f),
                                    ElectricIndigo.copy(alpha = 0.35f),
                                    NeonRose.copy(alpha = 0.25f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "Mping Protection Core",
                        tint = if (isConnected) LiquidCyan else TextMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mping",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHigh,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (vpnState.status) {
                                    ConnectionStatus.CONNECTED -> "SECURED"
                                    ConnectionStatus.CONNECTING -> "TUNNELING..."
                                    ConnectionStatus.DISCONNECTING -> "CLOSING"
                                    ConnectionStatus.DISCONNECTED -> "STANDBY"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // IP & Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val flag = when (vpnState.externalCountry) {
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
                            text = "$flag ${vpnState.externalIp}",
                            fontSize = 11.sp,
                            color = TextMedium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Real-Delay Latency HUD with Glowing Pulse Dot
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing LED dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                pingColor.copy(alpha = if (isConnected || isConnecting) pulseAlpha else 0.5f)
                            )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (vpnState.currentPingMs > 0) "${vpnState.currentPingMs} ms" else "-- ms",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pingColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onTestPingAll,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Test Delay All",
                        tint = LiquidCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
