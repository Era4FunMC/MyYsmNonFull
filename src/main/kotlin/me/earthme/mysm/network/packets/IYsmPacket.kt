package me.earthme.mysm.network.packets

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import org.bukkit.entity.Player

interface IYsmPacket {
    fun process(connectionType: EnumConnectionType,player: Player)

    fun writePacketData(dataBuf: ByteBuf,connectionType: EnumConnectionType)

    fun readPacketData(dataBuf: ByteBuf,connectionType: EnumConnectionType)

    fun getPacketId(): Int
}