package me.earthme.mysm.commands

import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetModelNeedAuthCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.model.management") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        if (args == null || args.size != 2){
            return false
        }



        val targetModel = NamespacedKey.fromString(args[0]) ?: return false
        val needAuth = if (args[1] == "true"){ true } else if (args[1] == "false"){ false } else { return false }

        if (!VersionedCacheLoader.hasLoadedModel(targetModel.key)){
            sender.sendMessage(ChatColor.RED.toString() + "Target model was not found!")
            return true
        }

        if (needAuth && ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(ChatColor.RED.toString() + "Target was already in the auth list!")
            return true
        }

        if (!needAuth && !ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(ChatColor.RED.toString() + "Target was already out of the auth list")
            return true
        }

        sender.sendMessage(ChatColor.GREEN.toString() + "Successfully executed!")
        MiscUtils.setModelNeedAuth(targetModel,needAuth)
        return true
    }
}