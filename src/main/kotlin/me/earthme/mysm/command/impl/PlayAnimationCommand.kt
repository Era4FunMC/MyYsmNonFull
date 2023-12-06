package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.utils.MiscUtils
import me.earthme.mysm.utils.YsmModelUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PlayAnimationCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission(PermissionConstants.cmdPlayAnimationOnPlayer)) {
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        if (args!!.size == 2){
            val targetPlayerName = args[0]
            val targetAnimationName = args[1]

            val targetPlayer = Bukkit.getPlayer(targetPlayerName)

            if (targetPlayer == null){
                sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_player_not_found"))
                return true
            }

            val currentPlayerData = PlayerDataManager.createOrGetPlayerData(targetPlayer.name)

            val currentModel = GlobalModelLoader.getTargetModelData(currentPlayerData.mainResourceLocation.key)!!
            val allAnimations = YsmModelUtils.getAnimationListFromModel(currentModel)

            if (!allAnimations.contains(targetAnimationName)){
                sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_animation_not_found"))
                val modelListMsg = StringBuffer()
                modelListMsg.append(I18nManager.parseTranslatableKey("commands.playanimationonplayer.animation_list_header")).append("\n")
                for (animationStr in allAnimations){
                    modelListMsg.append(I18nManager.parseTranslatableKey("commands.playanimationonplayer.single_entry_format",
                        arrayOf(animationStr)
                    )).append("\n")
                }
                sender.sendMessage(modelListMsg.toString())
                return true
            }

            MiscUtils.playAnimationOnPlayer(targetPlayer,targetAnimationName)
            return true
        }

        return false
    }
}