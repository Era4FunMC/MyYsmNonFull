package me.earthme.mysm.commands

import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.network.MainYsmNetworkHandler
import me.earthme.mysm.utils.MiscUtils
import me.earthme.mysm.utils.network.YsmPacketHelper
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GiveModelPlayerCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.gmodeltp") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        if (args == null || args.size != 2){
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val targetModel = NamespacedKey.fromString(args[1]) ?: return false

        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(ChatColor.RED.toString() + "Target player is not online or found!")
            return true
        }

        if (!ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(ChatColor.RED.toString() + "This model has not require auth yet!")
            return true
        }

        MiscUtils.giveModelToPlayer(targetPlayer,targetModel)
        sender.sendMessage(ChatColor.GREEN.toString()+"Successfully authed model $targetModel for player ${targetPlayer.name}")
        return true
    }
}