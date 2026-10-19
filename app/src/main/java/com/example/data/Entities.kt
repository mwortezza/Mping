package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: String,
    val serverAddress: String,
    val serverPort: Int,
    val uuidOrPassword: String,
    val security: String,
    val sni: String,
    val flow: String,
    val publicKey: String,
    val shortId: String,
    val network: String,
    val path: String,
    val countryCode: String,
    val countryName: String,
    val lastPingMs: Long,
    val isFavorite: Boolean,
    val subscriptionId: String?,
    val rxBytes: Long,
    val txBytes: Long
) {
    fun toDomain(): ServerProfile {
        val proto = try {
            ProxyProtocol.valueOf(protocol)
        } catch (e: Exception) {
            ProxyProtocol.VLESS
        }
        return ServerProfile(
            id = id,
            name = name,
            protocol = proto,
            serverAddress = serverAddress,
            serverPort = serverPort,
            uuidOrPassword = uuidOrPassword,
            security = security,
            sni = sni,
            flow = flow,
            publicKey = publicKey,
            shortId = shortId,
            network = network,
            path = path,
            countryCode = countryCode,
            countryName = countryName,
            lastPingMs = lastPingMs,
            isFavorite = isFavorite,
            subscriptionId = subscriptionId,
            rxBytes = rxBytes,
            txBytes = txBytes
        )
    }

    companion object {
        fun fromDomain(domain: ServerProfile): ServerEntity {
            return ServerEntity(
                id = domain.id,
                name = domain.name,
                protocol = domain.protocol.name,
                serverAddress = domain.serverAddress,
                serverPort = domain.serverPort,
                uuidOrPassword = domain.uuidOrPassword,
                security = domain.security,
                sni = domain.sni,
                flow = domain.flow,
                publicKey = domain.publicKey,
                shortId = domain.shortId,
                network = domain.network,
                path = domain.path,
                countryCode = domain.countryCode,
                countryName = domain.countryName,
                lastPingMs = domain.lastPingMs,
                isFavorite = domain.isFavorite,
                subscriptionId = domain.subscriptionId,
                rxBytes = domain.rxBytes,
                txBytes = domain.txBytes
            )
        }
    }
}

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val autoUpdate: Boolean,
    val updateIntervalHours: Int,
    val lastUpdated: Long,
    val serverCount: Int
)
