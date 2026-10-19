package com.example.engine

import com.example.model.ServerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object MpingPingManager {

    private const val SOCKET_TIMEOUT_MS = 3000

    suspend fun testServerDelay(server: ServerProfile): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var socket: Socket? = null
        try {
            socket = Socket()
            val socketAddress = InetSocketAddress(server.serverAddress, server.serverPort)
            socket.connect(socketAddress, SOCKET_TIMEOUT_MS)
            val elapsed = System.currentTimeMillis() - startTime
            elapsed.coerceAtLeast(1)
        } catch (e: Exception) {
            // If DNS/Connect fails, try a fast ICMP/HTTP probe to fallback or flag timeout
            -2L
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    suspend fun testAllConcurrent(
        servers: List<ServerProfile>,
        onProgress: (ServerProfile, Long) -> Unit
    ): Map<String, Long> = coroutineScope {
        val results = servers.map { server ->
            async {
                val delay = testServerDelay(server)
                onProgress(server, delay)
                server.id to delay
            }
        }.awaitAll().toMap()
        results
    }
}
