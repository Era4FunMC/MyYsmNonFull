package me.earthme.mysm.network.packets.s2c

import io.netty.buffer.ByteBuf
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.EnumConnectionType.*
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeUUID
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.writeVarInt
import me.earthme.mysm.utils.ysm.YsmNbtUtils
import org.bukkit.entity.Player

class YsmS2CEntityActionPacket(
    private val targetPlayer: Player
): IYsmPacket {
    override fun process(connectionType: EnumConnectionType,player: Player) {
        //We are not client
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        when(connectionType){
            FORGE -> {
                val modelData = PlayerDataManager.createOrGetPlayerData(this.targetPlayer.name)
                dataBuf.writeVarInt(this.targetPlayer.entityId)
                dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
            }

            FABRIC -> {
                val modelData = PlayerDataManager.createOrGetPlayerData(this.targetPlayer.name)
                dataBuf.writeVarInt(this.targetPlayer.entityId)
                dataBuf.writeUUID(this.targetPlayer.uniqueId)//Fabric only
                dataBuf.writeBytes(YsmNbtUtils.createNbtForSync(modelData))
            }

            VANILLA -> {

            }
        }
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not client
    }

    override fun getPacketId(): Int {
        return 4
    }
}