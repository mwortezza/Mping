package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConnectionStatus
import com.example.model.ServerProfile
import com.example.ui.MpingViewModel
import com.example.ui.components.AddNodeModal
import com.example.ui.components.FloatingGlassDock
import com.example.ui.components.FloatingGlassHud
import com.example.ui.components.MpingTab
import com.example.ui.components.SpatialFluidBackground
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NodesScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.TunnelCoreScreen
import com.example.ui.theme.MpingTheme
import com.example.ui.theme.SpaceBlack
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MpingTheme {
                MpingApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpingApp(
    viewModel: MpingViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val vpnState by viewModel.vpnState.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val activeServerId by viewModel.activeServerId.collectAsState()
    val isTestingPing by viewModel.isTestingPing.collectAsState()
    val downloadHistory by viewModel.downloadHistory.collectAsState()
    val uploadHistory by viewModel.uploadHistory.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val tunnelConfig by viewModel.tunnelConfig.collectAsState()

    var currentTab by remember { mutableStateOf(MpingTab.DASHBOARD) }
    var showAddNodeModal by remember { mutableStateOf(false) }
    var showServerMatrixSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // VPN Preparation Permission Result
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleVpn(context)
        } else {
            Toast.makeText(context, "VPN permission is required to create tunnel", Toast.LENGTH_SHORT).show()
        }
    }

    // Notification Permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun handleToggleVpn() {
        if (vpnState.status == ConnectionStatus.CONNECTED || vpnState.status == ConnectionStatus.CONNECTING) {
            viewModel.toggleVpn(context)
        } else {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnPrepareLauncher.launch(prepareIntent)
            } else {
                viewModel.toggleVpn(context)
            }
        }
    }

    // BackHandler: Return to Dashboard if on another tab
    BackHandler(enabled = currentTab != MpingTab.DASHBOARD) {
        currentTab = MpingTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpaceBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // 1. Spatial Fluid Background Engine (Dynamic 60/120fps orbs)
            val throughputMultiplier = ((vpnState.currentDownloadBps + vpnState.currentUploadBps) / (1024f * 1024f)).coerceIn(1f, 3f)
            SpatialFluidBackground(
                status = vpnState.status,
                throughputMultiplier = throughputMultiplier
            )

            // 2. Main Content Layout
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Floating Glass HUD
                FloatingGlassHud(
                    vpnState = vpnState,
                    onTestPingAll = { viewModel.testAllDelays() }
                )

                // Tab Content Transition
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            MpingTab.DASHBOARD -> DashboardScreen(
                                vpnState = vpnState,
                                activeServer = vpnState.activeServer,
                                downloadHistory = downloadHistory,
                                uploadHistory = uploadHistory,
                                onToggleVpn = { handleToggleVpn() },
                                onOpenServerMatrix = { showServerMatrixSheet = true }
                            )
                            MpingTab.NODES -> NodesScreen(
                                servers = servers,
                                activeServerId = activeServerId,
                                isTestingPing = isTestingPing,
                                onSelectServer = { server ->
                                    viewModel.selectServer(server)
                                    currentTab = MpingTab.DASHBOARD
                                },
                                onTestAllDelay = { viewModel.testAllDelays() },
                                onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                onDeleteServer = { id -> viewModel.deleteServer(id) },
                                onImportUri = { uri -> viewModel.importUri(uri) },
                                onOpenAddModal = { showAddNodeModal = true }
                            )
                            MpingTab.SUBSCRIPTIONS -> SubscriptionsScreen(
                                subscriptions = subscriptions,
                                isUpdating = false,
                                onAddSubscription = { title, url -> viewModel.addSubscription(title, url) },
                                onUpdateSubscription = { sub -> viewModel.updateSubscription(sub) },
                                onDeleteSubscription = { id -> viewModel.deleteSubscription(id) }
                            )
                            MpingTab.CORE -> TunnelCoreScreen(
                                tunnelConfig = tunnelConfig,
                                activeServer = vpnState.activeServer,
                                onUpdateConfig = { viewModel.updateTunnelConfig(it) }
                            )
                        }
                    }
                }
            }

            // 3. Floating iOS 27 Glass Dock (Suspended bottom capsule)
            FloatingGlassDock(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 4. Add Node Glass Modal
            if (showAddNodeModal) {
                AddNodeModal(
                    onDismiss = { showAddNodeModal = false },
                    onSaveProfile = { profile ->
                        viewModel.importUri(com.example.parser.MpingUriParser.toUri(profile))
                    },
                    onImportUri = { uri ->
                        viewModel.importUri(uri)
                    }
                )
            }

            // 5. Interactive Glass Bottom Sheet for Quick Server Matrix
            if (showServerMatrixSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showServerMatrixSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF070B12).copy(alpha = 0.95f),
                    scrimColor = Color.Black.copy(alpha = 0.65f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        NodesScreen(
                            servers = servers,
                            activeServerId = activeServerId,
                            isTestingPing = isTestingPing,
                            onSelectServer = { server ->
                                viewModel.selectServer(server)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showServerMatrixSheet = false
                                }
                            },
                            onTestAllDelay = { viewModel.testAllDelays() },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                            onDeleteServer = { id -> viewModel.deleteServer(id) },
                            onImportUri = { uri -> viewModel.importUri(uri) },
                            onOpenAddModal = {
                                showServerMatrixSheet = false
                                showAddNodeModal = true
                            }
                        )
                    }
                }
            }
        }
    }
}
