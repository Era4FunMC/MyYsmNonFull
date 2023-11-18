package me.earthme.mysm.network.connection

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

interface PlayerYsmConnection {
    fun isChannelNameMatched(channel: String): Boolean

    fun tick()

    fun onTrackerUpdate(see: Player)

    fun onPlayerJoin(player: Player)

    fun onPlayerLeft(player: Player)

    fun sendPacket(packet: IYsmPacket)

    fun getConnectionType(): EnumConnectionType
}