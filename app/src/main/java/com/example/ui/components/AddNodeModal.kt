package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import com.example.parser.MpingUriParser
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMuted
import java.util.UUID

@Composable
fun AddNodeModal(
    onDismiss: () -> Unit,
    onSaveProfile: (ServerProfile) -> Unit,
    onImportUri: (String) -> Unit
) {
    var rawUriInput by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("New Node") }
    var address by remember { mutableStateOf("") }
    var portStr by remember { mutableStateOf("443") }
    var uuidStr by remember { mutableStateOf("") }
    var sniStr by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        MpingLiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(26.dp),
            blurRadius = 30f,
            surfaceAlpha = 0.20f,
            hasChromaticGlow = true
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Add Proxy Node",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHigh
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Paste URI Section
                Text(
                    text = "FAST IMPORT (VLESS / TROJAN / HY2 / VMESS / SS)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidCyan,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rawUriInput,
                    onValueChange = { rawUriInput = it },
                    placeholder = { Text("Paste vless://, trojan://, or hy2:// link", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("paste_uri_field"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = TextHigh,
                        unfocusedTextColor = TextHigh
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Manual Section Divider
                Text(
                    text = "OR CONFIGURE MANUALLY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nodeName,
                    onValueChange = { nodeName = it },
                    label = { Text("Node Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = TextHigh
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / IP") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextHigh
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = portStr,
                        onValueChange = { portStr = it },
                        label = { Text("Port") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LiquidCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextHigh
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uuidStr,
                    onValueChange = { uuidStr = it },
                    label = { Text("UUID / Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = TextHigh
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sniStr,
                    onValueChange = { sniStr = it },
                    label = { Text("SNI (Server Name Indication)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LiquidCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = TextHigh
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (rawUriInput.isNotBlank()) {
                                onImportUri(rawUriInput)
                                onDismiss()
                            } else if (address.isNotBlank()) {
                                val manualProfile = ServerProfile(
                                    id = UUID.randomUUID().toString(),
                                    name = nodeName,
                                    protocol = ProxyProtocol.VLESS,
                                    serverAddress = address,
                                    serverPort = portStr.toIntOrNull() ?: 443,
                                    uuidOrPassword = uuidStr,
                                    security = "reality",
                                    sni = sniStr,
                                    flow = "xtls-rprx-vision"
                                )
                                onSaveProfile(manualProfile)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiquidCyan),
                        modifier = Modifier.testTag("confirm_add_node_button")
                    ) {
                        Text("Save Node", color = SpaceBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
