package me.earthme.mysm.commands

import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.MainYsmNetworkHandler
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ListPlayersCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.listysmplayers") && !sender.isOp){
            sender.sendMessage(ChatColor.RED.toString() + "You do not have permission to execute this command!")
            return true
        }

        val builder: StringBuilder = java.lang.StringBuilder()
        for (singlePlayer in MainYsmNetworkHandler.getModInstalledPlayers()){
            builder.append("${ChatColor.GOLD}Player ${ChatColor.GREEN.toString() + singlePlayer.name} is using model ${ChatColor.LIGHT_PURPLE.toString() + PlayerDataManager.createOrGetPlayerData(singlePlayer.name).mainResourceLocation}").append("\n")
        }
        sender.sendMessage(builder.toString())
        return true
    }
}