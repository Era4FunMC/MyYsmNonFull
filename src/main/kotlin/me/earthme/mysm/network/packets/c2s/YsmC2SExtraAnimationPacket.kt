package me.earthme.mysm.network.packets.c2s

import io.netty.buffer.ByteBuf
import me.earthme.mysm.data.PlayerModelData
import me.earthme.mysm.events.PlayerExtraAnimationEvent
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.utils.api.MiscUtils
import org.bukkit.entity.Player

class YsmC2SExtraAnimationPacket: IYsmPacket {
    private var animationId: Int = 0

    override fun process(connectionType: EnumConnectionType, player: Player) {
        if (-1 <= this.animationId && this.animationId < 8 && PlayerExtraAnimationEvent(player,this.animationId).callEvent()) {
            val currentHeld: PlayerModelData = PlayerDataManager.createOrGetPlayerData(player.name)
            currentHeld.sendAnimation = true //Set send latch to true to make the players around could see the player's animation
            if (this.animationId != -1) {
                MiscUtils.playAnimationOnPlayer(player,"extra$animationId")
            } else {
                currentHeld.doAnimation = false
            }
        }
    }

    override fun writePacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        //We are not server to send this packet
    }

    override fun readPacketData(dataBuf: ByteBuf, connectionType: EnumConnectionType) {
        this.animationId = dataBuf.readInt()
    }

    override fun getPacketId(): Int {
        return 7
    }
}