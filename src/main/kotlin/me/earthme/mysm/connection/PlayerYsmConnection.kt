package me.earthme.mysm.connection

import io.netty.buffer.ByteBuf
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

interface PlayerYsmConnection {
    fun tick()

    fun onTrackerUpdate(see: Player)

    fun onPlayerJoin(player: Player)

    fun onPlayerLeft(player: Player)

    fun sendPacket(packetData: ByteBuf,packetId: NamespacedKey)

    fun onMessageIncoming(key: NamespacedKey,packetData: ByteArray)
}