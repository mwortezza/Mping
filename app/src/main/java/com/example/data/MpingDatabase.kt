package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.model.ProxyProtocol
import com.example.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY isFavorite DESC, lastPingMs ASC, name ASC")
    fun getAllServersFlow(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :id LIMIT 1")
    suspend fun getServerById(id: String): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<ServerEntity>)

    @Update
    suspend fun updateServer(server: ServerEntity)

    @Query("UPDATE servers SET lastPingMs = :pingMs WHERE id = :id")
    suspend fun updatePing(id: String, pingMs: Long)

    @Query("UPDATE servers SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Delete
    suspend fun deleteServer(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServerById(id: String)

    @Query("DELETE FROM servers")
    suspend fun clearAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY lastUpdated DESC")
    fun getAllSubscriptionsFlow(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(sub: SubscriptionEntity)
}

@Database(entities = [ServerEntity::class, SubscriptionEntity::class], version = 1, exportSchema = false)
abstract class MpingDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: MpingDatabase? = null

        fun getDatabase(context: Context): MpingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MpingDatabase::class.java,
                    "mping_liquid_core.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun seedDefaultServers(): List<ServerProfile> {
            return listOf(
                ServerProfile(
                    id = "mping-node-us-01",
                    name = "US-East (Cloudflare Reality / Vision)",
                    protocol = ProxyProtocol.VLESS,
                    serverAddress = "104.16.132.229",
                    serverPort = 443,
                    uuidOrPassword = "a3b98d41-7681-4328-98e2-82dc35b67812",
                    security = "reality",
                    sni = "gateway.icloud.com",
                    flow = "xtls-rprx-vision",
                    publicKey = "2x37aL8mJ79aN82vQ1Z5rV9xLmO3pQ4a",
                    shortId = "6ba85301",
                    network = "tcp",
                    countryCode = "US",
                    countryName = "United States",
                    lastPingMs = 32,
                    isFavorite = true
                ),
                ServerProfile(
                    id = "mping-node-de-02",
                    name = "Frankfurt Turbo (VLESS Reality)",
                    protocol = ProxyProtocol.VLESS,
                    serverAddress = "172.67.181.90",
                    serverPort = 443,
                    uuidOrPassword = "b7c09e52-8792-5439-09f3-93ed46c78923",
                    security = "reality",
                    sni = "www.microsoft.com",
                    flow = "xtls-rprx-vision",
                    publicKey = "8y49bM9nK80bO93wR2A6sW0yMnP4qR5b",
                    shortId = "7cb96412",
                    network = "tcp",
                    countryCode = "DE",
                    countryName = "Germany",
                    lastPingMs = 48,
                    isFavorite = true
                ),
                ServerProfile(
                    id = "mping-node-sg-03",
                    name = "Singapore Equinix (Trojan TLS)",
                    protocol = ProxyProtocol.TROJAN,
                    serverAddress = "104.18.25.10",
                    serverPort = 443,
                    uuidOrPassword = "mping_sg_secure_trojan_token",
                    security = "tls",
                    sni = "sg-node.mping.space",
                    network = "tcp",
                    countryCode = "SG",
                    countryName = "Singapore",
                    lastPingMs = 54,
                    isFavorite = false
                ),
                ServerProfile(
                    id = "mping-node-jp-04",
                    name = "Tokyo Ginza (Hysteria 2 UDP)",
                    protocol = ProxyProtocol.HYSTERIA2,
                    serverAddress = "104.21.80.11",
                    serverPort = 8443,
                    uuidOrPassword = "mping_tokyo_hy2_pass",
                    security = "tls",
                    sni = "jp.mping.space",
                    countryCode = "JP",
                    countryName = "Japan",
                    lastPingMs = 68,
                    isFavorite = false
                ),
                ServerProfile(
                    id = "mping-node-gb-05",
                    name = "London Telehouse (VMess WS TLS)",
                    protocol = ProxyProtocol.VMESS,
                    serverAddress = "104.26.12.8",
                    serverPort = 443,
                    uuidOrPassword = "c8d10f63-9803-6540-10a4-04fe57d89034",
                    security = "tls",
                    sni = "uk.mping.space",
                    network = "ws",
                    path = "/mping-speed",
                    countryCode = "GB",
                    countryName = "United Kingdom",
                    lastPingMs = 76,
                    isFavorite = false
                ),
                ServerProfile(
                    id = "mping-node-nl-06",
                    name = "Amsterdam Fiber (Shadowsocks AEAD)",
                    protocol = ProxyProtocol.SHADOWSOCKS,
                    serverAddress = "172.64.150.12",
                    serverPort = 8388,
                    uuidOrPassword = "aes-256-gcm:MpingSecretPassword2026",
                    security = "none",
                    countryCode = "NL",
                    countryName = "Netherlands",
                    lastPingMs = 82,
                    isFavorite = false
                )
            )
        }
    }
}
