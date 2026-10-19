package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMuted

enum class MpingTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Rounded.AllInclusive),
    NODES("Nodes", Icons.Rounded.Hub),
    SUBSCRIPTIONS("Sub Groups", Icons.Rounded.CloudSync),
    CORE("Tunnel Core", Icons.Rounded.Tune)
}

@Composable
fun FloatingGlassDock(
    currentTab: MpingTab,
    onTabSelected: (MpingTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = MpingTab.values()
    val activeIndex = tabs.indexOf(currentTab).toFloat()

    val animatedIndex by animateFloatAsState(
        targetValue = activeIndex,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_slider"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        MpingLiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            blurRadius = 35f,
            surfaceAlpha = 0.14f
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(6.dp)
            ) {
                val tabWidth = maxWidth / tabs.size

                // Sliding Liquid-Glass Active Pill Indicator
                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * animatedIndex)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    LiquidCyan.copy(alpha = 0.20f),
                                    ElectricIndigo.copy(alpha = 0.25f)
                                )
                            )
                        )
                )

                // Tab Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == currentTab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("tab_${tab.name.lowercase()}")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(tab) }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) LiquidCyan else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextHigh else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
