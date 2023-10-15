package me.earthme.mysm.connection

import io.netty.buffer.ByteBuf
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

interface PlayerYsmConnection {
    fun isChannelNameMatched(channel: String): Boolean

    fun tick()

    fun onTrackerUpdate(see: Player)

    fun onPlayerJoin(player: Player)

    fun onPlayerLeft(player: Player)

    fun sendPacket(packetData: ByteBuf,packetId: NamespacedKey)

    fun onMessageIncoming(key: NamespacedKey,packetData: ByteArray)

    fun sendHeldModes(models: Set<NamespacedKey>)

    fun sendReload()

    fun sendModelUpdate(ownerEntity: Player)

    fun sendMd5Contained(containedMd5: String)

    fun sendModelOrPasswordData(data: ByteArray)
}