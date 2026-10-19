package com.example.engine

import com.example.model.DnsProvider
import com.example.model.ProxyProtocol
import com.example.model.RoutingMode
import com.example.model.ServerProfile
import com.example.model.TunnelConfig
import org.json.JSONArray
import org.json.JSONObject

object XrayConfigGenerator {

    fun generateXrayJson(profile: ServerProfile, config: TunnelConfig): String {
        val root = JSONObject()

        // 1. Log configuration
        val log = JSONObject().apply {
            put("loglevel", "warning")
            put("dnsLog", false)
        }
        root.put("log", log)

        // 2. Inbound: SOCKS5 & HTTP & Dokodemo-door (TUN)
        val inbounds = JSONArray()
        val socksInbound = JSONObject().apply {
            put("tag", "socks-in")
            put("port", 10808)
            put("listen", "127.0.0.1")
            put("protocol", "socks")
            put("settings", JSONObject().apply {
                put("auth", "noauth")
                put("udp", true)
            })
            put("sniffing", JSONObject().apply {
                put("enabled", config.enableSniffing)
                put("destOverride", JSONArray().put("http").put("tls").put("quic"))
            })
        }
        val httpInbound = JSONObject().apply {
            put("tag", "http-in")
            put("port", 10809)
            put("listen", "127.0.0.1")
            put("protocol", "http")
        }
        inbounds.put(socksInbound)
        inbounds.put(httpInbound)
        root.put("inbounds", inbounds)

        // 3. Outbounds: Active Proxy + Direct + Block
        val outbounds = JSONArray()
        val proxyOutbound = JSONObject().apply {
            put("tag", "proxy")
            when (profile.protocol) {
                ProxyProtocol.VLESS -> {
                    put("protocol", "vless")
                    val vnext = JSONArray().put(JSONObject().apply {
                        put("address", profile.serverAddress)
                        put("port", profile.serverPort)
                        put("users", JSONArray().put(JSONObject().apply {
                            put("id", profile.uuidOrPassword)
                            put("encryption", "none")
                            if (profile.flow.isNotEmpty()) put("flow", profile.flow)
                        }))
                    })
                    put("settings", JSONObject().put("vnext", vnext))
                    put("streamSettings", buildStreamSettings(profile, config))
                }
                ProxyProtocol.VMESS -> {
                    put("protocol", "vmess")
                    val vnext = JSONArray().put(JSONObject().apply {
                        put("address", profile.serverAddress)
                        put("port", profile.serverPort)
                        put("users", JSONArray().put(JSONObject().apply {
                            put("id", profile.uuidOrPassword)
                            put("alterId", 0)
                            put("security", "auto")
                        }))
                    })
                    put("settings", JSONObject().put("vnext", vnext))
                    put("streamSettings", buildStreamSettings(profile, config))
                }
                ProxyProtocol.TROJAN -> {
                    put("protocol", "trojan")
                    val servers = JSONArray().put(JSONObject().apply {
                        put("address", profile.serverAddress)
                        put("port", profile.serverPort)
                        put("password", profile.uuidOrPassword)
                    })
                    put("settings", JSONObject().put("servers", servers))
                    put("streamSettings", buildStreamSettings(profile, config))
                }
                ProxyProtocol.SHADOWSOCKS -> {
                    put("protocol", "shadowsocks")
                    val servers = JSONArray().put(JSONObject().apply {
                        put("address", profile.serverAddress)
                        put("port", profile.serverPort)
                        put("password", profile.uuidOrPassword)
                        put("method", "aes-256-gcm")
                    })
                    put("settings", JSONObject().put("servers", servers))
                }
                ProxyProtocol.HYSTERIA2 -> {
                    put("protocol", "hysteria2")
                    put("settings", JSONObject().apply {
                        put("server", "${profile.serverAddress}:${profile.serverPort}")
                        put("auth", profile.uuidOrPassword)
                        put("tls", JSONObject().apply {
                            put("sni", if (profile.sni.isNotEmpty()) profile.sni else profile.serverAddress)
                            put("insecure", config.allowInsecure)
                        })
                    })
                }
                ProxyProtocol.WIREGUARD -> {
                    put("protocol", "wireguard")
                    put("settings", JSONObject().apply {
                        put("secretKey", profile.uuidOrPassword)
                        val peers = JSONArray().put(JSONObject().apply {
                            put("endpoint", "${profile.serverAddress}:${profile.serverPort}")
                            put("publicKey", profile.publicKey)
                        })
                        put("peers", peers)
                    })
                }
            }
        }
        val directOutbound = JSONObject().apply {
            put("tag", "direct")
            put("protocol", "freedom")
        }
        val blockOutbound = JSONObject().apply {
            put("tag", "block")
            put("protocol", "blackhole")
        }
        outbounds.put(proxyOutbound)
        outbounds.put(directOutbound)
        outbounds.put(blockOutbound)
        root.put("outbounds", outbounds)

        // 4. Routing Table
        val routing = JSONObject().apply {
            put("domainStrategy", "IPIfNonMatch")
            val rules = JSONArray()

            // AdBlock rule
            rules.put(JSONObject().apply {
                put("type", "field")
                put("outboundTag", "block")
                put("domain", JSONArray().put("geosite:category-ads-all"))
            })

            // Routing mode rules
            if (config.routingMode == RoutingMode.BYPASS_LAN_CN) {
                rules.put(JSONObject().apply {
                    put("type", "field")
                    put("outboundTag", "direct")
                    put("ip", JSONArray().put("geoip:private").put("geoip:cn"))
                })
                rules.put(JSONObject().apply {
                    put("type", "field")
                    put("outboundTag", "direct")
                    put("domain", JSONArray().put("geosite:cn"))
                })
            }

            // Default proxy rule
            rules.put(JSONObject().apply {
                put("type", "field")
                put("outboundTag", "proxy")
                put("network", "tcp,udp")
            })

            put("rules", rules)
        }
        root.put("routing", routing)

        // 5. DNS config
        val dns = JSONObject().apply {
            val servers = JSONArray()
            when (config.dnsProvider) {
                DnsProvider.CLOUDFLARE -> {
                    servers.put(config.dnsProvider.dohUrl)
                    servers.put("1.1.1.1")
                }
                DnsProvider.GOOGLE -> {
                    servers.put(config.dnsProvider.dohUrl)
                    servers.put("8.8.8.8")
                }
                DnsProvider.ADGUARD -> {
                    servers.put(config.dnsProvider.dohUrl)
                    servers.put("94.140.14.14")
                }
            }
            put("servers", servers)
        }
        root.put("dns", dns)

        return root.toString(2)
    }

    private fun buildStreamSettings(profile: ServerProfile, config: TunnelConfig): JSONObject {
        val stream = JSONObject()
        stream.put("network", if (profile.network.isNotEmpty()) profile.network else "tcp")

        if (profile.security == "reality") {
            stream.put("security", "reality")
            val realitySettings = JSONObject().apply {
                put("show", false)
                put("serverName", if (profile.sni.isNotEmpty()) profile.sni else profile.serverAddress)
                put("fingerprint", "chrome")
                put("publicKey", profile.publicKey)
                put("shortId", profile.shortId)
                put("spiderX", "")
            }
            stream.put("realitySettings", realitySettings)
        } else if (profile.security == "tls") {
            stream.put("security", "tls")
            val tlsSettings = JSONObject().apply {
                put("serverName", if (profile.sni.isNotEmpty()) profile.sni else profile.serverAddress)
                put("allowInsecure", config.allowInsecure)
                put("alpn", JSONArray().put("h2").put("http/1.1"))
            }
            stream.put("tlsSettings", tlsSettings)
        }

        if (profile.network == "ws") {
            val wsSettings = JSONObject().apply {
                put("path", if (profile.path.isNotEmpty()) profile.path else "/")
                val headers = JSONObject()
                if (profile.sni.isNotEmpty()) headers.put("Host", profile.sni)
                put("headers", headers)
            }
            stream.put("wsSettings", wsSettings)
        }

        return stream
    }
}
