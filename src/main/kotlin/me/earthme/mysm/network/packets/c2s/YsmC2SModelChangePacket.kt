package me.earthme.mysm.network.packets.c2s

import io.netty.buffer.ByteBuf
import me.earthme.mysm.events.PlayerChangeModelEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CEntityActionPacket
import me.earthme.mysm.utils.api.MiscUtils
import me.earthme.mysm.utils.mc.MCPacketCodecUtils.readResourceLocation
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class YsmC2SModelChangePacket : IYsmPacket{
    private var modelLocation: NamespacedKey? = null
    private var modelTextureLocation: NamespacedKey? = null

    override fun process(connectionType: EnumConnectionType, player: Player) {
        val playerChangeModelEvent = PlayerChangeModelEvent(player,this.modelLocation!!,this.modelTextureLocation!!)

        //Check model access
        if ((!ModelPermissionManager.isModelNeedAuth(this.modelLocation!!) || ModelPermissionManager.isPlayerHeldModel(player,this.modelLocation!!)) && playerChangeModelEvent.callEvent()){
            MiscUtils.setModelForPlayer(player,this.modelLocation!!,this.modelTextureLocation!!)
        }else{
            player.getConnection()!!.sendPacket(YsmS2CEntityActionPacket(player)) //Correct the player model it currently has
        }
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not server to send this packet
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        this.modelLocation = dataBuf.readResourceLocation()
        this.modelTextureLocation = dataBuf.readResourceLocation()
    }

    override fun getPacketId(): Int {
        return 5
    }
}