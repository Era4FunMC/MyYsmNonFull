package me.earthme.mysm.utils

import me.earthme.mysm.events.PlayerAnimationEvent
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.network.YsmClientConnectionManager.getConnection
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

/**
 * 算是暴露在外的API部分罢()
 * 用于一些杂七杂八的插件功能和外部调用
 */
object MiscUtils {
    /**
     * 重载全部的模型
     */
    fun reloadAllModels(){
        VersionedCacheLoader.reloadCaches()
        GlobalModelLoader.reloadAll()
    }

    /**
     * 用于设置模型是否要授权才能使用，可以随时设置
     * @param modelLocation 模型的名字，只需要key符合即可
     * @param needAuth 是否要授权才能使用
     * @exception IllegalArgumentException 如果没有找到对应模型会抛出这个
     */
    fun setModelNeedAuth(modelLocation: NamespacedKey, needAuth: Boolean){
        GlobalModelLoader.getTargetModelData(modelLocation.key)?.let{
            ModelPermissionManager.setModelNeedAuth(modelLocation,needAuth)
            VersionedCacheLoader.refreshCache(it)
            YsmClientConnectionManager.sendReloadToAllPlayers()
            return
        }

        throw IllegalArgumentException("Target model has not found!")
    }

    /**
     * 取消对玩家的模型授权，这个模型一般要被设置为需要授权后才能使用
     * @param targetPlayer 目标玩家
     * @param targetModel 目标模型，需要namespace为yes_steve_model，key符合即可
     */
    fun dropModelForPlayer(targetPlayer: Player,targetModel: NamespacedKey){
        ModelPermissionManager.removePlayerHeldModel(targetPlayer,targetModel)
        targetPlayer.getConnection()?.sendHeldModes(ModelPermissionManager.getHeldModelsOfPlayer(targetPlayer))
        PlayerDataManager.setToDefaultIfIncorrect(targetPlayer) //Correct if the current model is removed
        PlayerDataManager.createOrGetPlayerData(targetPlayer.name).sendAnimation = true //Set send latch to true
    }

    /**
     * 授权给玩家目标模型
     * @param targetPlayer 目标玩家
     * @param targetModel 目标模型，需要namespace为yes_steve_model，key符合即可
     */
    fun giveModelToPlayer(targetPlayer: Player,targetModel: NamespacedKey){
        ModelPermissionManager.addPlayerHeldModel(targetPlayer,targetModel)
        targetPlayer.getConnection()?.sendHeldModes(ModelPermissionManager.getHeldModelsOfPlayer(targetPlayer))
    }

    /**
     * 在某个玩家身上播放他正在使用的模型上的动画，这个功能受PlayerAnimationEvent制约
     * @param player 目标玩家
     * @param animation 动画名称
     */
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

    /**
     * 设置一个玩家的模型
     * @param player 指定玩家
     * @param modelLocation 指定模型
     */
    fun setModelForPlayer(player: Player,modelLocation: NamespacedKey){
        val targetData = PlayerDataManager.createOrGetPlayerData(player.name)

        val targetModelData = GlobalModelLoader.getTargetModelData(player.name)
        for((fileName, _) in targetModelData!!.getAllFiles()){
            if (fileName.endsWith(".png")){
                targetData.mainTextPngResourceLocation = NamespacedKey.fromString("$modelLocation/$fileName")!!
            }
        }

        targetData.mainResourceLocation = modelLocation
        targetData.isDirty = true

        for (singlePlayer in Bukkit.getOnlinePlayers()){
            singlePlayer.getConnection()?.sendModelUpdate(player)
        }
    }

    /**
     * 设置一个玩家的模型
     * @param player 目标玩家
     * @param modelLocation 指定模型
     * @param textureLocation 指定模型的材质文件的路径
     */
    fun setModelForPlayer(player: Player,modelLocation: NamespacedKey,textureLocation: NamespacedKey){
        val targetData = PlayerDataManager.createOrGetPlayerData(player.name)
        targetData.mainResourceLocation = modelLocation
        targetData.mainTextPngResourceLocation = textureLocation
        targetData.isDirty = true

        for (singlePlayer in Bukkit.getOnlinePlayers()){
            singlePlayer.getConnection()?.sendModelUpdate(player)
        }
    }
}