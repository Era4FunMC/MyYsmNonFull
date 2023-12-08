package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeUtf
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeVarInt
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class YsmS2COwnedModelListPacket(
    private val heldModes: Set<NamespacedKey>
): IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {
        //We are not client
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        dataBuf.writeVarInt(this.heldModes.size)
        for (model in this.heldModes){
            dataBuf.writeUtf(model.toString(),32767)
        }
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 6
    }
}