package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeUtf
import org.bukkit.entity.Player

class YsmS2CCacheHitPacket(
    private val md5Str: String
) : IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {
        //We are not client
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        dataBuf.writeUtf(this.md5Str,32767)
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 3
    }
}