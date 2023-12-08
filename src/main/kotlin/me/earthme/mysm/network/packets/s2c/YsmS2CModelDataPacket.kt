package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeVarInt
import org.bukkit.entity.Player

class YsmS2CModelDataPacket(
    private val modelData: ByteArray
): IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {} //We are not client

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        dataBuf.writeVarInt(this.modelData.size)
        dataBuf.writeBytes(this.modelData)
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 1
    }
}