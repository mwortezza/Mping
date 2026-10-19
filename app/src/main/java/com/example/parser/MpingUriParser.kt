package com.example.parser

import android.net.Uri
import android.util.Base64
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

object MpingUriParser {

    fun parse(rawUriString: String): ServerProfile? {
        val trimmed = rawUriString.trim()
        return try {
            when {
                trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed)
                trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed)
                trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed)
                trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed)
                trimmed.startsWith("hysteria2://", ignoreCase = true) || trimmed.startsWith("hy2://", ignoreCase = true) -> parseHysteria2(trimmed)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseVless(uriStr: String): ServerProfile {
        val uri = Uri.parse(uriStr)
        val userInfo = uri.userInfo ?: ""
        val host = uri.host ?: "127.0.0.1"
        val port = if (uri.port > 0) uri.port else 443
        val fragment = uri.fragment?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) } ?: "VLESS Node"
        val security = uri.getQueryParameter("security") ?: "reality"
        val sni = uri.getQueryParameter("sni") ?: ""
        val flow = uri.getQueryParameter("flow") ?: "xtls-rprx-vision"
        val pbk = uri.getQueryParameter("pbk") ?: ""
        val sid = uri.getQueryParameter("sid") ?: ""
        val type = uri.getQueryParameter("type") ?: "tcp"
        val path = uri.getQueryParameter("path") ?: ""

        val (cc, cn) = detectCountry(host, fragment)

        return ServerProfile(
            id = UUID.randomUUID().toString(),
            name = fragment,
            protocol = ProxyProtocol.VLESS,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = userInfo,
            security = security,
            sni = sni,
            flow = flow,
            publicKey = pbk,
            shortId = sid,
            network = type,
            path = path,
            countryCode = cc,
            countryName = cn
        )
    }

    private fun parseTrojan(uriStr: String): ServerProfile {
        val uri = Uri.parse(uriStr)
        val password = uri.userInfo ?: ""
        val host = uri.host ?: "127.0.0.1"
        val port = if (uri.port > 0) uri.port else 443
        val fragment = uri.fragment?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) } ?: "Trojan Node"
        val sni = uri.getQueryParameter("sni") ?: uri.getQueryParameter("peer") ?: host
        val type = uri.getQueryParameter("type") ?: "tcp"
        val (cc, cn) = detectCountry(host, fragment)

        return ServerProfile(
            id = UUID.randomUUID().toString(),
            name = fragment,
            protocol = ProxyProtocol.TROJAN,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = password,
            security = "tls",
            sni = sni,
            network = type,
            countryCode = cc,
            countryName = cn
        )
    }

    private fun parseVmess(uriStr: String): ServerProfile? {
        val b64 = uriStr.removePrefix("vmess://").trim()
        val decoded = try {
            String(Base64.decode(b64, Base64.DEFAULT or Base64.URL_SAFE), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            return null
        }
        val json = JSONObject(decoded)
        val host = json.optString("add", "127.0.0.1")
        val port = json.optInt("port", 443)
        val id = json.optString("id", "")
        val remark = json.optString("ps", "VMess Node")
        val net = json.optString("net", "tcp")
        val tls = json.optString("tls", "none")
        val sni = json.optString("sni", host)
        val path = json.optString("path", "")
        val (cc, cn) = detectCountry(host, remark)

        return ServerProfile(
            id = UUID.randomUUID().toString(),
            name = remark,
            protocol = ProxyProtocol.VMESS,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = id,
            security = tls,
            sni = sni,
            network = net,
            path = path,
            countryCode = cc,
            countryName = cn
        )
    }

    private fun parseShadowsocks(uriStr: String): ServerProfile {
        val stripped = uriStr.removePrefix("ss://").trim()
        val parts = stripped.split("#", limit = 2)
        val remark = if (parts.size > 1) URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()) else "Shadowsocks"
        val mainPart = parts[0]

        var host = "127.0.0.1"
        var port = 8388
        var password = ""

        if (mainPart.contains("@")) {
            val atSplit = mainPart.split("@", limit = 2)
            val creds = String(Base64.decode(atSplit[0], Base64.DEFAULT or Base64.URL_SAFE), StandardCharsets.UTF_8)
            password = creds
            val hostPort = atSplit[1].split(":", limit = 2)
            host = hostPort[0]
            if (hostPort.size > 1) port = hostPort[1].toIntOrNull() ?: 8388
        } else {
            val decoded = String(Base64.decode(mainPart, Base64.DEFAULT or Base64.URL_SAFE), StandardCharsets.UTF_8)
            if (decoded.contains("@")) {
                val atSplit = decoded.split("@", limit = 2)
                password = atSplit[0]
                val hostPort = atSplit[1].split(":", limit = 2)
                host = hostPort[0]
                if (hostPort.size > 1) port = hostPort[1].toIntOrNull() ?: 8388
            }
        }

        val (cc, cn) = detectCountry(host, remark)
        return ServerProfile(
            id = UUID.randomUUID().toString(),
            name = remark,
            protocol = ProxyProtocol.SHADOWSOCKS,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = password,
            security = "none",
            countryCode = cc,
            countryName = cn
        )
    }

    private fun parseHysteria2(uriStr: String): ServerProfile {
        val uri = Uri.parse(uriStr)
        val password = uri.userInfo ?: ""
        val host = uri.host ?: "127.0.0.1"
        val port = if (uri.port > 0) uri.port else 443
        val fragment = uri.fragment?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) } ?: "Hysteria 2"
        val sni = uri.getQueryParameter("sni") ?: host
        val (cc, cn) = detectCountry(host, fragment)

        return ServerProfile(
            id = UUID.randomUUID().toString(),
            name = fragment,
            protocol = ProxyProtocol.HYSTERIA2,
            serverAddress = host,
            serverPort = port,
            uuidOrPassword = password,
            security = "tls",
            sni = sni,
            countryCode = cc,
            countryName = cn
        )
    }

    fun parseSubscriptionContent(content: String): List<ServerProfile> {
        val trimmed = content.trim()
        val plainText = try {
            String(Base64.decode(trimmed, Base64.DEFAULT or Base64.URL_SAFE), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            trimmed
        }

        return plainText.lines()
            .mapNotNull { line ->
                val lineTrimmed = line.trim()
                if (lineTrimmed.isNotEmpty()) parse(lineTrimmed) else null
            }
    }

    fun toUri(profile: ServerProfile): String {
        return when (profile.protocol) {
            ProxyProtocol.VLESS -> {
                "vless://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}?type=${profile.network}&security=${profile.security}&sni=${profile.sni}&flow=${profile.flow}&pbk=${profile.publicKey}&sid=${profile.shortId}#${Uri.encode(profile.name)}"
            }
            ProxyProtocol.TROJAN -> {
                "trojan://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}?security=${profile.security}&sni=${profile.sni}#${Uri.encode(profile.name)}"
            }
            ProxyProtocol.HYSTERIA2 -> {
                "hysteria2://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}?sni=${profile.sni}#${Uri.encode(profile.name)}"
            }
            ProxyProtocol.SHADOWSOCKS -> {
                val encodedCreds = Base64.encodeToString(profile.uuidOrPassword.toByteArray(), Base64.NO_WRAP)
                "ss://$encodedCreds@${profile.serverAddress}:${profile.serverPort}#${Uri.encode(profile.name)}"
            }
            ProxyProtocol.VMESS -> {
                val json = JSONObject().apply {
                    put("v", "2")
                    put("ps", profile.name)
                    put("add", profile.serverAddress)
                    put("port", profile.serverPort)
                    put("id", profile.uuidOrPassword)
                    put("net", profile.network)
                    put("type", "none")
                    put("tls", profile.security)
                    put("sni", profile.sni)
                    put("path", profile.path)
                }
                val b64 = Base64.encodeToString(json.toString().toByteArray(), Base64.NO_WRAP)
                "vmess://$b64"
            }
            ProxyProtocol.WIREGUARD -> {
                "wireguard://${profile.uuidOrPassword}@${profile.serverAddress}:${profile.serverPort}#${Uri.encode(profile.name)}"
            }
        }
    }

    private fun detectCountry(host: String, remark: String): Pair<String, String> {
        val lower = (remark + " " + host).lowercase()
        return when {
            lower.contains("us") || lower.contains("united states") || lower.contains("america") || lower.contains("los angeles") || lower.contains("california") || lower.contains("virginia") -> "US" to "United States"
            lower.contains("de") || lower.contains("germany") || lower.contains("frankfurt") || lower.contains("deutschland") -> "DE" to "Germany"
            lower.contains("jp") || lower.contains("japan") || lower.contains("tokyo") || lower.contains("osaka") -> "JP" to "Japan"
            lower.contains("sg") || lower.contains("singapore") -> "SG" to "Singapore"
            lower.contains("gb") || lower.contains("uk") || lower.contains("united kingdom") || lower.contains("london") -> "GB" to "United Kingdom"
            lower.contains("nl") || lower.contains("netherlands") || lower.contains("amsterdam") -> "NL" to "Netherlands"
            lower.contains("ca") || lower.contains("canada") || lower.contains("toronto") -> "CA" to "Canada"
            lower.contains("fr") || lower.contains("france") || lower.contains("paris") -> "FR" to "France"
            lower.contains("hk") || lower.contains("hong kong") -> "HK" to "Hong Kong"
            lower.contains("kr") || lower.contains("korea") || lower.contains("seoul") -> "KR" to "South Korea"
            else -> "US" to "United States"
        }
    }
}
