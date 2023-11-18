package me.earthme.mysm.network.coders

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.EnumConnectionType.*
import me.earthme.mysm.network.packets.IYsmPacket
import org.bukkit.NamespacedKey

object YsmPacketEncoder {
    fun encodeYsmPacket(ysmPacket: IYsmPacket,connectionType: EnumConnectionType):Pair<NamespacedKey,ByteBuf>{
        val packetBuffer = Unpooled.buffer()
        var channelName = NamespacedKey("yes_steve_model","unknown")

        when(connectionType){
            FORGE -> {
                channelName = NamespacedKey("yes_steve_model","network")
                packetBuffer.writeByte(ysmPacket.getPacketId() or 255)
                ysmPacket.writePacketData(packetBuffer,connectionType)
            }

            FABRIC -> {
                channelName = NamespacedKey("yes_steve_model",ysmPacket.getPacketId().toString())
                ysmPacket.writePacketData(packetBuffer,connectionType)
            }

            VANILLA -> {}
        }

        return Pair(channelName,packetBuffer)
    }
}