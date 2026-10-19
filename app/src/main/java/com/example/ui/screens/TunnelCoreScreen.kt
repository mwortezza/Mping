package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.XrayConfigGenerator
import com.example.model.DnsProvider
import com.example.model.RoutingMode
import com.example.model.ServerProfile
import com.example.model.TunnelConfig
import com.example.ui.components.MpingLiquidGlassCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted

@Composable
fun TunnelCoreScreen(
    tunnelConfig: TunnelConfig,
    activeServer: ServerProfile?,
    onUpdateConfig: (TunnelConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    var showJsonModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = "Tunnel Core",
                tint = LiquidCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Headless Tunnel Core",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHigh
                )
                Text(
                    text = "Xray-core / sing-box headless engine parameters",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Routing Mode Card
        MpingLiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            blurRadius = 25f,
            surfaceAlpha = 0.10f
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Route,
                        contentDescription = "Routing Mode",
                        tint = LiquidCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Routing Strategy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHigh
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                RoutingMode.values().forEach { mode ->
                    val isSelected = tunnelConfig.routingMode == mode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) LiquidCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) LiquidCyan.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onUpdateConfig(tunnelConfig.copy(routingMode = mode)) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = mode.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) LiquidCyan else TextHigh
                            )
                            Text(
                                text = mode.subtitle,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // DNS Provider Card
        MpingLiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            blurRadius = 25f,
            surfaceAlpha = 0.10f
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Dns,
                        contentDescription = "DNS Provider",
                        tint = LiquidCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encrypted DNS (DoH)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextHigh
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                DnsProvider.values().forEach { provider ->
                    val isSelected = tunnelConfig.dnsProvider == provider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) LiquidCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) LiquidCyan.copy(alpha = 0.6f) else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onUpdateConfig(tunnelConfig.copy(dnsProvider = provider)) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = provider.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) LiquidCyan else TextHigh
                                )
                                Text(
                                    text = provider.primaryIp,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Toggles & Optimizations
        MpingLiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            blurRadius = 25f,
            surfaceAlpha = 0.10f
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Core Engine Toggles",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHigh
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Sniffing
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TLS & HTTP Sniffing", fontSize = 13.sp, color = TextHigh)
                        Text("Extract domain for intelligent routing and DNS bypass", fontSize = 10.sp, color = TextMuted)
                    }
                    Switch(
                        checked = tunnelConfig.enableSniffing,
                        onCheckedChange = { onUpdateConfig(tunnelConfig.copy(enableSniffing = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiquidCyan, checkedTrackColor = ElectricIndigo)
                    )
                }

                // Mux
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mux (Multiplexing)", fontSize = 13.sp, color = TextHigh)
                        Text("Combine multiple TCP streams to reduce handshake latency", fontSize = 10.sp, color = TextMuted)
                    }
                    Switch(
                        checked = tunnelConfig.enableMux,
                        onCheckedChange = { onUpdateConfig(tunnelConfig.copy(enableMux = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiquidCyan, checkedTrackColor = ElectricIndigo)
                    )
                }

                // Allow Insecure
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Insecure TLS", fontSize = 13.sp, color = TextHigh)
                        Text("Skip TLS certificate verification (development only)", fontSize = 10.sp, color = TextMuted)
                    }
                    Switch(
                        checked = tunnelConfig.allowInsecure,
                        onCheckedChange = { onUpdateConfig(tunnelConfig.copy(allowInsecure = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonRose, checkedTrackColor = ElectricIndigo)
                    )
                }
            }
        }

        // View & Export Raw Headless Xray JSON
        activeServer?.let { server ->
            val generatedJson = remember(server, tunnelConfig) {
                XrayConfigGenerator.generateXrayJson(server, tunnelConfig)
            }

            MpingLiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                blurRadius = 25f,
                surfaceAlpha = 0.12f,
                hasChromaticGlow = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = "Config JSON",
                                tint = LiquidCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Headless Core JSON Config",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHigh
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedJson))
                                Toast.makeText(context, "Xray JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_xray_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy JSON",
                                tint = LiquidCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF030508).copy(alpha = 0.85f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = generatedJson.lines().take(15).joinToString("\n") + "\n...",
                            fontSize = 11.sp,
                            color = LiquidCyan.copy(alpha = 0.85f),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
