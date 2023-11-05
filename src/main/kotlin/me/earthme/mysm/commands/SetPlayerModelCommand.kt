package me.earthme.mysm.commands

import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetPlayerModelCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.smodelfp") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        if (args!!.size != 2){
            return false
        }

        val targetModelLocation = NamespacedKey.fromString(args[1]) ?: return false
        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(ChatColor.RED.toString() + "Target player was not found or already offline!")
            return true
        }

        if (!VersionedCacheLoader.hasLoadedModel(args[1])){
            sender.sendMessage(ChatColor.RED.toString() + "Target model was not found!")
            return true
        }

        MiscUtils.setModelForPlayer(targetPlayer,targetModelLocation)

        return true
    }
}