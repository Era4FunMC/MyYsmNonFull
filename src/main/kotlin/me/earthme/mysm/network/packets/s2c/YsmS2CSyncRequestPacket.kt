package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import org.bukkit.entity.Player

class YsmS2CSyncRequestPacket: IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {
        //We are not client
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //There is no data we need to write in
        dataBuf.writeByte(1)
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 2
    }
}