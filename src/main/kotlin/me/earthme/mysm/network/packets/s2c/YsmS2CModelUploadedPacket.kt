package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import org.bukkit.entity.Player

class YsmS2CModelUploadedPacket: IYsmPacket {
    override fun process(connectionType: EnumConnectionType, player: Player) {
        //We are not client to process this packet
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //No data
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client to read this packet
    }

    override fun getPacketId(): Int {
        return 12
    }
}