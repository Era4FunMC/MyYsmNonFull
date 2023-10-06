package me.earthme.mysm.commands

import me.earthme.mysm.manager.MultiSupportedVersionCacheManager
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.MainYsmNetworkHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadModelsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.reload") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        MultiSupportedVersionCacheManager.reload()
        for (player in Bukkit.getOnlinePlayers()){
            PlayerDataManager.setToDefaultIfIncorrect(player)
        }
        MainYsmNetworkHandler.sendReloadToAllPlayers()
        sender.sendMessage(ChatColor.GREEN.toString() + "Successfully reload all models!")
        return true
    }
}