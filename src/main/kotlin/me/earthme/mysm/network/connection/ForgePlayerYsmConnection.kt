package me.earthme.mysm.network.connection

import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.EnumConnectionType
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import me.earthme.mysm.network.YsmClientConnectionManager.sendCustomPayLoad
import me.earthme.mysm.network.coders.YsmPacketEncoder
import me.earthme.mysm.network.packets.IYsmPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CEntityActionPacket
import me.earthme.mysm.network.packets.s2c.YsmS2COwnedModelListPacket
import me.earthme.mysm.network.packets.s2c.YsmS2CSyncRequestPacket
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

class ForgePlayerYsmConnection(
    private val player: Player
): PlayerYsmConnection {
    private val connectionType = EnumConnectionType.fromConnection(this)

    companion object {
        private val CHANNEL = NamespacedKey("yes_steve_model", "network")
    }

    override fun isChannelNameMatched(channel: String): Boolean {
        return CHANNEL.toString() == channel
    }

    override fun tick() {
        val playerData = PlayerDataManager.createOrGetPlayerData(this.player.name)
        if (playerData.sendAnimation){
            for (singlePlayer in Bukkit.getOnlinePlayers()){
                singlePlayer.getConnection()?.sendPacket(YsmS2CEntityActionPacket(this.player))
            }
            playerData.sendAnimation = false
        }
    }

    override fun onTrackerUpdate(see: Player) {
        this.sendPacket(YsmS2CEntityActionPacket(see))
    }

    override fun onPlayerJoin(player: Player) {
        //Send reload & model update
        this.sendPacket(YsmS2CSyncRequestPacket())
        //Set the player model to default before sync
        PlayerDataManager.setToDefaultIfIncorrect(this.player)
        //Sync models the players currently held
        this.sendPacket(YsmS2COwnedModelListPacket(ModelPermissionManager.getHeldModelsOfPlayer(this.player)))
        //Sync current model
        this.sendPacket(YsmS2CEntityActionPacket(this.player))
    }

    override fun onPlayerLeft(player: Player) {
        //Remove player from the installed list
        YsmClientConnectionManager.modInstalledPlayerList.remove(player)
    }

    override fun sendPacket(packet: IYsmPacket) {
        val encoded = YsmPacketEncoder.encodeYsmPacket(packet,this.connectionType)
        this.player.sendCustomPayLoad(encoded.first,encoded.second)
    }

    override fun getConnectionType(): EnumConnectionType {
        return this.connectionType
    }
}