package com.example.model

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

data class VpnState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedDurationSeconds: Long = 0,
    val currentUploadBps: Long = 0,
    val currentDownloadBps: Long = 0,
    val totalTxBytes: Long = 0,
    val totalRxBytes: Long = 0,
    val externalIp: String = "104.28.19.42",
    val externalCountry: String = "US",
    val currentPingMs: Long = 28,
    val activeServer: ServerProfile? = null
)
