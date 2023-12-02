package me.earthme.mysm.network

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage
import io.netty.buffer.ByteBuf
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object YsmPacketHelper {
    fun sendCustomPayLoad(target: Player, channel: NamespacedKey, data: ByteBuf){
        val byteArray = ByteArray(data.readableBytes())
        data.readBytes(byteArray)
        PacketEvents.getAPI().playerManager.sendPacket(target,WrapperPlayServerPluginMessage(channel.toString(),byteArray))
    }
}