package com.example.model

data class Subscription(
    val id: String,
    val title: String,
    val url: String,
    val autoUpdate: Boolean = true,
    val updateIntervalHours: Int = 24,
    val lastUpdated: Long = System.currentTimeMillis(),
    val serverCount: Int = 0
)

enum class RoutingMode(val displayName: String, val subtitle: String) {
    GLOBAL("Global Tunnel", "Route all device traffic through Mping VPN"),
    BYPASS_LAN_CN("Bypass LAN & China", "Directly access local network and mainland services"),
    CUSTOM("Smart Rules", "Custom domain and IP CIDR routing table")
}

enum class DnsProvider(val displayName: String, val primaryIp: String, val dohUrl: String) {
    CLOUDFLARE("Cloudflare DoH", "1.1.1.1", "https://cloudflare-dns.com/dns-query"),
    GOOGLE("Google DoH", "8.8.8.8", "https://dns.google/dns-query"),
    ADGUARD("AdGuard AdBlock", "94.140.14.14", "https://dns.adguard.com/dns-query")
}

data class TunnelConfig(
    val routingMode: RoutingMode = RoutingMode.BYPASS_LAN_CN,
    val dnsProvider: DnsProvider = DnsProvider.CLOUDFLARE,
    val mtu: Int = 1500,
    val enableSniffing: Boolean = true,
    val enableMux: Boolean = false,
    val allowInsecure: Boolean = false,
    val enableIpv6: Boolean = false,
    val perAppProxyEnabled: Boolean = false
)
