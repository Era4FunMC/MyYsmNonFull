package me.earthme.mysm.utils

import me.earthme.mysm.events.PlayerAnimationEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object MiscUtils {
    fun reloadAllModels(){
        VersionedCacheLoader.reload()
        GlobalModelLoader.reloadAll()
    }

    fun setModelNeedAuth(modelLocation: NamespacedKey, needAuth: Boolean){
        GlobalModelLoader.getTargetModelData(modelLocation.key)?.let{
            ModelPermissionManager.setModelNeedAuth(modelLocation,needAuth)
            VersionedCacheLoader.refreshCache(it)
            YsmClientConnectionManager.sendReloadToAllPlayers()
            return
        }

        throw IllegalArgumentException("Target model has not found!")
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