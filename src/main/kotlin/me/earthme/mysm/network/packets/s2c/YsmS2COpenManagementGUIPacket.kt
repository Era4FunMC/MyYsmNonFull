package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.data.mod.management.YsmModelDesc
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeVarInt
import org.bukkit.entity.Player

class YsmS2COpenManagementGUIPacket (
    private val customModels: Set<YsmModelDesc>,
    private val authModels: Set<YsmModelDesc>
): IYsmPacket{
    override fun process(connectionType: EnumConnectionType, player: Player) {
        //We are not client to process this
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        dataBuf.writeVarInt(this.customModels.size)
        for (singleEntry in this.customModels){
            singleEntry.writeToBuffer(dataBuf)
        }

        dataBuf.writeVarInt(this.authModels.size)
        for (singleEntry in this.authModels){
            singleEntry.writeToBuffer(dataBuf)
        }
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client to read this packet
    }

    override fun getPacketId(): Int {
        return 10
    }
}