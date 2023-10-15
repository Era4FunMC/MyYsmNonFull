package me.earthme.mysm.utils

import me.earthme.mysm.events.PlayerAnimationEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.manager.MultiSupportedVersionCacheManager
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.MainYsmNetworkHandler
import me.earthme.mysm.network.MainYsmNetworkHandler.getConnection
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object MiscUtils {
    fun setModelNeedAuth(modelLocation: NamespacedKey, needAuth: Boolean){
        ModelPermissionManager.setModelNeedAuth(modelLocation,needAuth)
        MultiSupportedVersionCacheManager.refreshCache(modelLocation.key)
        MainYsmNetworkHandler.sendReloadToAllPlayers()
    }

    fun dropModelForPlayer(targetPlayer: Player,targetModel: NamespacedKey){
        ModelPermissionManager.removePlayerHeldModel(targetPlayer,targetModel)
        targetPlayer.getConnection()?.sendHeldModes(ModelPermissionManager.getHeldModelsOfPlayer(targetPlayer))
        PlayerDataManager.setToDefaultIfIncorrect(targetPlayer) //Correct if the current model is removed
        PlayerDataManager.createOrGetPlayerData(targetPlayer.name).sendAnimation = true //Set send latch to true
    }

    fun giveModelToPlayer(targetPlayer: Player,targetModel: NamespacedKey){
        ModelPermissionManager.addPlayerHeldModel(targetPlayer,targetModel)
        targetPlayer.getConnection()?.sendHeldModes(ModelPermissionManager.getHeldModelsOfPlayer(targetPlayer))
    }

    fun playAnimationOnPlayer(player: Player,animation: String){
        val playerAnimationEvent = PlayerAnimationEvent(player,animation)

        if (!playerAnimationEvent.callEvent()){
            return
        }

        val playerData = PlayerDataManager.createOrGetPlayerData(player.name)
        playerData.currentAnimation = animation
        playerData.doAnimation = true
        playerData.sendAnimation = true
    }
}