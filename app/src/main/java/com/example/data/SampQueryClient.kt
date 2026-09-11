package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class SampServerLiveInfo(
    val isOnline: Boolean,
    val isPasswordLocked: Boolean,
    val hostname: String,
    val ip: String = SampQueryClient.OFFICIAL_IP,
    val port: Int = SampQueryClient.OFFICIAL_PORT,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val gamemode: String,
    val mapname: String,
    val ping: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SampOnlinePlayer(
    val id: Int,
    val name: String,
    val score: Int,
    val ping: Int
)

object SampQueryClient {
    const val OFFICIAL_IP = "51.79.254.10"
    const val OFFICIAL_PORT = 7774
    const val DEFAULT_HOSTNAME = "NEXSTON ROLEPLAY | SRI LANKA"

    suspend fun queryServer(ip: String = OFFICIAL_IP, port: Int = OFFICIAL_PORT): SampServerLiveInfo =
        withContext(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            val startTime = System.currentTimeMillis()
            try {
                socket = DatagramSocket()
                socket.soTimeout = 2800

                val address = InetAddress.getByName(ip)
                val ipParts = ip.split(".").map { it.toInt() }

                // SAMP packet: 'SAMP' + 4 IP bytes + 2 port bytes (little endian) + 'i'
                val sendBuf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                sendBuf.put('S'.code.toByte())
                sendBuf.put('A'.code.toByte())
                sendBuf.put('M'.code.toByte())
                sendBuf.put('P'.code.toByte())
                for (part in ipParts) {
                    sendBuf.put(part.toByte())
                }
                sendBuf.putShort(port.toShort())
                sendBuf.put('i'.code.toByte())

                val sendPacket = DatagramPacket(sendBuf.array(), sendBuf.capacity(), address, port)
                socket.send(sendPacket)

                val recvBuf = ByteArray(2048)
                val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
                socket.receive(recvPacket)
                val ping = (System.currentTimeMillis() - startTime).coerceAtLeast(1)

                val buffer = ByteBuffer.wrap(recvPacket.data, 0, recvPacket.length).order(ByteOrder.LITTLE_ENDIAN)
                if (recvPacket.length >= 11) {
                    buffer.position(11) // Skip 'SAMP' + IP + Port + 'i'
                    val passwordByte = buffer.get()
                    val isLocked = passwordByte.toInt() != 0
                    val players = buffer.short.toInt() and 0xFFFF
                    val maxPlayers = buffer.short.toInt() and 0xFFFF

                    val hostnameLen = buffer.int
                    val safeHostLen = hostnameLen.coerceIn(0, 256)
                    val hostnameBytes = ByteArray(safeHostLen)
                    buffer.get(hostnameBytes)
                    val hostname = String(hostnameBytes, Charsets.ISO_8859_1).trim()

                    val gamemodeLen = buffer.int
                    val safeGameLen = gamemodeLen.coerceIn(0, 256)
                    val gamemodeBytes = ByteArray(safeGameLen)
                    buffer.get(gamemodeBytes)
                    val gamemode = String(gamemodeBytes, Charsets.ISO_8859_1).trim()

                    val mapLen = buffer.int
                    val safeMapLen = mapLen.coerceIn(0, 256)
                    val mapBytes = ByteArray(safeMapLen)
                    buffer.get(mapBytes)
                    val mapname = String(mapBytes, Charsets.ISO_8859_1).trim()

                    SampServerLiveInfo(
                        isOnline = true,
                        isPasswordLocked = isLocked,
                        hostname = if (hostname.isNotBlank()) hostname else DEFAULT_HOSTNAME,
                        ip = ip,
                        port = port,
                        currentPlayers = players,
                        maxPlayers = if (maxPlayers > 0) maxPlayers else 100,
                        gamemode = if (gamemode.isNotBlank()) gamemode else "NEXSTON",
                        mapname = if (mapname.isNotBlank()) mapname else "Sri Lanka",
                        ping = ping,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    throw IllegalStateException("Packet too short")
                }
            } catch (e: Exception) {
                SampServerLiveInfo(
                    isOnline = false,
                    isPasswordLocked = false,
                    hostname = DEFAULT_HOSTNAME,
                    ip = ip,
                    port = port,
                    currentPlayers = 0,
                    maxPlayers = 100,
                    gamemode = "NEXSTON",
                    mapname = "Sri Lanka",
                    ping = 0,
                    lastUpdated = System.currentTimeMillis()
                )
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }

    suspend fun queryDetailedPlayers(ip: String = OFFICIAL_IP, port: Int = OFFICIAL_PORT): List<SampOnlinePlayer> =
        withContext(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.soTimeout = 2800

                val address = InetAddress.getByName(ip)
                val ipParts = ip.split(".").map { it.toInt() }

                val sendBuf = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)
                sendBuf.put('S'.code.toByte())
                sendBuf.put('A'.code.toByte())
                sendBuf.put('M'.code.toByte())
                sendBuf.put('P'.code.toByte())
                for (part in ipParts) {
                    sendBuf.put(part.toByte())
                }
                sendBuf.putShort(port.toShort())
                sendBuf.put('d'.code.toByte())

                val sendPacket = DatagramPacket(sendBuf.array(), sendBuf.capacity(), address, port)
                socket.send(sendPacket)

                val recvBuf = ByteArray(4096)
                val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
                socket.receive(recvPacket)

                val buffer = ByteBuffer.wrap(recvPacket.data, 0, recvPacket.length).order(ByteOrder.LITTLE_ENDIAN)
                if (recvPacket.length >= 13) {
                    buffer.position(11)
                    val count = buffer.short.toInt() and 0xFFFF
                    val playersList = mutableListOf<SampOnlinePlayer>()
                    for (i in 0 until count) {
                        if (buffer.remaining() < 2) break
                        val playerId = buffer.get().toInt() and 0xFF
                        val nameLen = buffer.get().toInt() and 0xFF
                        if (buffer.remaining() < nameLen + 8) break
                        val nameBytes = ByteArray(nameLen)
                        buffer.get(nameBytes)
                        val name = String(nameBytes, Charsets.ISO_8859_1)
                        val score = buffer.int
                        val ping = buffer.int
                        playersList.add(SampOnlinePlayer(playerId, name, score, ping))
                    }
                    playersList
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
}
