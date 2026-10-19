package com.example.model

enum class ProxyProtocol(val displayName: String, val scheme: String) {
    VLESS("VLESS", "vless"),
    VMESS("VMess", "vmess"),
    TROJAN("Trojan", "trojan"),
    SHADOWSOCKS("Shadowsocks", "ss"),
    HYSTERIA2("Hysteria 2", "hy2"),
    WIREGUARD("WireGuard", "wg")
}

data class ServerProfile(
    val id: String,
    val name: String,
    val protocol: ProxyProtocol,
    val serverAddress: String,
    val serverPort: Int,
    val uuidOrPassword: String,
    val security: String = "tls", // tls, reality, none
    val sni: String = "",
    val flow: String = "xtls-rprx-vision", // for reality
    val publicKey: String = "",           // reality public key
    val shortId: String = "",             // reality short id
    val network: String = "tcp",          // tcp, ws, grpc, quic
    val path: String = "",
    val countryCode: String = "US",
    val countryName: String = "United States",
    val lastPingMs: Long = -1,            // -1 = unmeasured, -2 = timeout/error
    val isFavorite: Boolean = false,
    val subscriptionId: String? = null,
    val rxBytes: Long = 0,
    val txBytes: Long = 0
) {
    fun getPingStatus(): PingGrade {
        return when {
            lastPingMs in 0..79 -> PingGrade.ULTRA_FAST
            lastPingMs in 80..149 -> PingGrade.GOOD
            lastPingMs in 150..299 -> PingGrade.FAIR
            lastPingMs >= 300 -> PingGrade.SLOW
            else -> PingGrade.UNTESTED
        }
    }
}

enum class PingGrade {
    ULTRA_FAST,
    GOOD,
    FAIR,
    SLOW,
    UNTESTED
}
