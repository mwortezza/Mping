package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MpingDatabase
import com.example.data.ServerEntity
import com.example.data.SubscriptionEntity
import com.example.engine.MpingPingManager
import com.example.model.ConnectionStatus
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import com.example.model.Subscription
import com.example.model.TunnelConfig
import com.example.model.VpnState
import com.example.parser.MpingUriParser
import com.example.service.MpingVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.TimeUnit

class MpingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MpingDatabase.getDatabase(application)
    private val serverDao = db.serverDao()
    private val subscriptionDao = db.subscriptionDao()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Active Selected Server ID
    private val _activeServerId = MutableStateFlow<String?>("mping-node-us-01")
    val activeServerId: StateFlow<String?> = _activeServerId.asStateFlow()

    // Tunnel Engine Configuration
    private val _tunnelConfig = MutableStateFlow(TunnelConfig())
    val tunnelConfig: StateFlow<TunnelConfig> = _tunnelConfig.asStateFlow()

    // Live Ping Testing Flag
    private val _isTestingPing = MutableStateFlow(false)
    val isTestingPing: StateFlow<Boolean> = _isTestingPing.asStateFlow()

    // Subscriptions List
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    // Speed Graph sliding window history (20 points for smooth Bezier interpolation)
    private val _downloadHistory = MutableStateFlow<List<Float>>(List(20) { 0f })
    val downloadHistory: StateFlow<List<Float>> = _downloadHistory.asStateFlow()

    private val _uploadHistory = MutableStateFlow<List<Float>>(List(20) { 0f })
    val uploadHistory: StateFlow<List<Float>> = _uploadHistory.asStateFlow()

    // Server Profiles from Room
    val servers: StateFlow<List<ServerProfile>> = serverDao.getAllServersFlow()
        .combine(_activeServerId) { serverEntities, _ ->
            serverEntities.map { it.toDomain() }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Unified VPN State Flow
    private val _vpnState = MutableStateFlow(VpnState())
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    init {
        // Seed default high-speed nodes if database is freshly created
        viewModelScope.launch(Dispatchers.IO) {
            val initial = serverDao.getServerById("mping-node-us-01")
            if (initial == null) {
                val seedList = MpingDatabase.seedDefaultServers().map { ServerEntity.fromDomain(it) }
                serverDao.insertServers(seedList)
            }
        }

        // Collect subscriptions
        viewModelScope.launch {
            subscriptionDao.getAllSubscriptionsFlow().collectLatest { entities ->
                _subscriptions.value = entities.map {
                    Subscription(
                        id = it.id,
                        title = it.title,
                        url = it.url,
                        autoUpdate = it.autoUpdate,
                        updateIntervalHours = it.updateIntervalHours,
                        lastUpdated = it.lastUpdated,
                        serverCount = it.serverCount
                    )
                }
            }
        }

        // Bridge MpingVpnService StateFlow into ViewModel StateFlow
        viewModelScope.launch {
            val liveMetricsFlow = combine(
                MpingVpnService.serviceStatus,
                MpingVpnService.rxSpeed,
                MpingVpnService.txSpeed,
                MpingVpnService.totalRx,
                MpingVpnService.totalTx
            ) { status, rx, tx, totalRx, totalTx ->
                LiveMetrics(status, rx, tx, totalRx, totalTx)
            }

            combine(
                liveMetricsFlow,
                MpingVpnService.uptimeSeconds,
                servers,
                _activeServerId
            ) { metrics, uptime, serverList, activeId ->
                val active = serverList.firstOrNull { it.id == activeId } ?: serverList.firstOrNull()

                // Update speed curves sliding window
                val currentRxKbs = (metrics.rx / 1024f).coerceAtLeast(0f)
                val currentTxKbs = (metrics.tx / 1024f).coerceAtLeast(0f)

                _downloadHistory.value = (_downloadHistory.value.drop(1) + currentRxKbs)
                _uploadHistory.value = (_uploadHistory.value.drop(1) + currentTxKbs)

                VpnState(
                    status = metrics.status,
                    connectedDurationSeconds = uptime,
                    currentUploadBps = metrics.tx,
                    currentDownloadBps = metrics.rx,
                    totalTxBytes = metrics.totalTx,
                    totalRxBytes = metrics.totalRx,
                    externalIp = if (metrics.status == ConnectionStatus.CONNECTED) (active?.serverAddress ?: "104.28.19.42") else "104.28.19.42",
                    externalCountry = active?.countryCode ?: "US",
                    currentPingMs = active?.lastPingMs ?: 28,
                    activeServer = active
                )
            }.collectLatest { state ->
                _vpnState.value = state
            }
        }
    }

    fun toggleVpn(context: Context) {
        val currentStatus = _vpnState.value.status
        if (currentStatus == ConnectionStatus.CONNECTED || currentStatus == ConnectionStatus.CONNECTING) {
            MpingVpnService.stopVpn(context)
        } else {
            val serverName = _vpnState.value.activeServer?.name ?: "Mping Liquid Core"
            MpingVpnService.startVpn(context, serverName)
        }
    }

    fun selectServer(server: ServerProfile) {
        _activeServerId.value = server.id
        // If connected, seamless switch
        if (_vpnState.value.status == ConnectionStatus.CONNECTED) {
            MpingVpnService.startVpn(getApplication(), server.name)
        }
    }

    fun testAllDelays() {
        viewModelScope.launch(Dispatchers.IO) {
            _isTestingPing.value = true
            val currentList = servers.value
            MpingPingManager.testAllConcurrent(currentList) { server, delayMs ->
                viewModelScope.launch(Dispatchers.IO) {
                    serverDao.updatePing(server.id, delayMs)
                }
            }
            _isTestingPing.value = false
        }
    }

    fun importUri(rawUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if it's a subscription base64 list or single URI
            val parsedList = MpingUriParser.parseSubscriptionContent(rawUri)
            if (parsedList.isNotEmpty()) {
                val entities = parsedList.map { ServerEntity.fromDomain(it) }
                serverDao.insertServers(entities)
            } else {
                val single = MpingUriParser.parse(rawUri)
                if (single != null) {
                    serverDao.insertServer(ServerEntity.fromDomain(single))
                    _activeServerId.value = single.id
                }
            }
        }
    }

    fun toggleFavorite(id: String, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.updateFavorite(id, isFav)
        }
    }

    fun deleteServer(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            serverDao.deleteServerById(id)
        }
    }

    fun updateTunnelConfig(config: TunnelConfig) {
        _tunnelConfig.value = config
    }

    fun addSubscription(title: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val subId = UUID.randomUUID().toString()
            val entity = SubscriptionEntity(
                id = subId,
                title = title,
                url = url,
                autoUpdate = true,
                updateIntervalHours = 24,
                lastUpdated = System.currentTimeMillis(),
                serverCount = 0
            )
            subscriptionDao.insertSubscription(entity)

            // Fetch and parse feed immediately
            fetchAndParseSubscription(subId, url)
        }
    }

    fun updateSubscription(sub: Subscription) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchAndParseSubscription(sub.id, sub.url)
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            subscriptionDao.deleteSubscription(
                SubscriptionEntity(id, "", "", false, 24, 0, 0)
            )
        }
    }

    private suspend fun fetchAndParseSubscription(subId: String, url: String) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotEmpty()) {
                val parsed = MpingUriParser.parseSubscriptionContent(body)
                val entities = parsed.map {
                    ServerEntity.fromDomain(it.copy(subscriptionId = subId))
                }
                serverDao.insertServers(entities)
                // Update count
                val updated = SubscriptionEntity(
                    id = subId,
                    title = "Sync Feed",
                    url = url,
                    autoUpdate = true,
                    updateIntervalHours = 24,
                    lastUpdated = System.currentTimeMillis(),
                    serverCount = entities.size
                )
                subscriptionDao.insertSubscription(updated)
            }
        } catch (_: Exception) {}
    }
}

private data class LiveMetrics(
    val status: ConnectionStatus,
    val rx: Long,
    val tx: Long,
    val totalRx: Long,
    val totalTx: Long
)
