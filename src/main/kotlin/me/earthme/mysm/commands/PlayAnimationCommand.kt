package me.earthme.mysm.commands

import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class PlayAnimationCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.playanimationonplayer") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        if (args!!.size == 2){
            val targetPlayerName = args[0]
            val targetAnimationName = args[1]

            val targetPlayer = Bukkit.getPlayer(targetPlayerName)

            if (targetPlayer == null){
                sender.sendMessage(ChatColor.RED.toString() + "Target player has not found!")
                return true
            }

            val currentPlayerData = PlayerDataManager.createOrGetPlayerData(targetPlayer.name)
            //TODO Complete it
            //val currentModel = ModelLoader.getModel(currentPlayerData.mainResourceLocation)!!
            //val allAnimations = YsmModelUtils.getAnimationListFromModel(currentModel)

            /*if (!allAnimations.contains(targetAnimationName)){
                sender.sendMessage(ChatColor.RED.toString() + "Target animation not found!")
                val modelListMsg = StringBuffer()
                modelListMsg
                    .append("All models of this model:").append("\n")
                for (modelStr in allAnimations){
                    modelListMsg.append(modelStr).append("\n")
                }
                sender.sendMessage(modelListMsg.toString())
                return true
            }*/

            MiscUtils.playAnimationOnPlayer(targetPlayer,targetAnimationName)
            return true
        }

        return false
    }
}