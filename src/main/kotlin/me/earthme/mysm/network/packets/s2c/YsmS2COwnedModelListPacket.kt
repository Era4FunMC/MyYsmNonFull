package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class YsmS2COwnedModelListPacket(
    private val heldModes: Set<NamespacedKey>
): IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {
        //We are not client
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        MCPacketCodecUtils.writeVarInt(this.heldModes.size,dataBuf)
        for (model in this.heldModes){
            MCPacketCodecUtils.writeUtf(model.toString(),32767,dataBuf)
        }
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 6
    }
}