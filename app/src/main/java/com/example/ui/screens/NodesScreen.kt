package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PingGrade
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import com.example.parser.MpingUriParser
import com.example.ui.components.MpingLiquidGlassCard
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.GlassBorderHighlight
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
fun NodesScreen(
    servers: List<ServerProfile>,
    activeServerId: String?,
    isTestingPing: Boolean,
    onSelectServer: (ServerProfile) -> Unit,
    onTestAllDelay: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDeleteServer: (String) -> Unit,
    onImportUri: (String) -> Unit,
    onOpenAddModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredServers = remember(servers, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> servers
            "VLESS" -> servers.filter { it.protocol == ProxyProtocol.VLESS }
            "TROJAN" -> servers.filter { it.protocol == ProxyProtocol.TROJAN }
            "HYSTERIA2" -> servers.filter { it.protocol == ProxyProtocol.HYSTERIA2 }
            "VMESS" -> servers.filter { it.protocol == ProxyProtocol.VMESS }
            "FAVORITES" -> servers.filter { it.isFavorite }
            "FAST" -> servers.filter { it.lastPingMs in 1..100 }
            else -> servers
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        // Control Bar: Real-Delay Test & Import Actions
        MpingLiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            blurRadius = 25f,
            surfaceAlpha = 0.12f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // One-Tap Real-Delay Test Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    LiquidCyan.copy(alpha = 0.20f),
                                    ElectricIndigo.copy(alpha = 0.30f)
                                )
                            )
                        )
                        .border(1.dp, LiquidCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isTestingPing) { onTestAllDelay() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("test_all_delay_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTestingPing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LiquidCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TESTING PINGS...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = "Test Real-Delay",
                                tint = LiquidCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MPING REAL-DELAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHigh,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Import / Add Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text ?: ""
                            if (clipText.isNotBlank()) {
                                onImportUri(clipText)
                                Toast.makeText(context, "Node imported from clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("clipboard_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Paste URI",
                            tint = LiquidCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenAddModal,
                        modifier = Modifier.testTag("add_node_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Node",
                            tint = TextHigh,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            listOf("ALL", "VLESS", "TROJAN", "HYSTERIA2", "VMESS", "FAST", "FAVORITES").forEach { tag ->
                val isSelected = selectedFilter == tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) LiquidCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) LiquidCyan.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedFilter = tag }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LiquidCyan else TextMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Server Matrix Deck List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredServers, key = { it.id }) { server ->
                val isSelected = server.id == activeServerId

                val protoColor = when (server.protocol) {
                    ProxyProtocol.VLESS -> LiquidCyan
                    ProxyProtocol.TROJAN -> ElectricIndigo
                    ProxyProtocol.HYSTERIA2 -> NeonRose
                    ProxyProtocol.VMESS -> Color(0xFFB5179E)
                    ProxyProtocol.SHADOWSOCKS -> Color(0xFFF77F00)
                    ProxyProtocol.WIREGUARD -> Color(0xFF4CC9F0)
                }

                val pingColor = when {
                    server.lastPingMs in 1..79 -> PingUltraFast
                    server.lastPingMs in 80..149 -> PingGood
                    server.lastPingMs in 150..299 -> PingFair
                    server.lastPingMs >= 300 -> PingSlow
                    else -> TextMuted
                }

                MpingLiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectServer(server) }
                        .testTag("server_card_${server.id}"),
                    shape = RoundedCornerShape(20.dp),
                    blurRadius = 20f,
                    surfaceAlpha = if (isSelected) 0.18f else 0.08f,
                    hasChromaticGlow = isSelected
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Radio & Node details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "Select Node",
                                tint = if (isSelected) LiquidCyan else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        text = "$flag ${server.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextHigh,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(protoColor.copy(alpha = 0.16f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = server.protocol.displayName,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = protoColor
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

                        // Delay badge & quick actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Delay Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pingColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (server.lastPingMs > 0) "${server.lastPingMs} ms" else if (server.lastPingMs == -2L) "TIMEOUT" else "-- ms",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pingColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Favorite toggle
                            IconButton(
                                onClick = { onToggleFavorite(server.id, !server.isFavorite) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (server.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (server.isFavorite) Color(0xFFFFB703) else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Copy URI
                            IconButton(
                                onClick = {
                                    val uri = MpingUriParser.toUri(server)
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uri))
                                    Toast.makeText(context, "Node URI copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Copy URI",
                                    tint = TextMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Delete
                            IconButton(
                                onClick = { onDeleteServer(server.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete Node",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
