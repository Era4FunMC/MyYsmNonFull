package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.YsmClientConnectionManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ListPlayersCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.listysmplayers") && !sender.isOp){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        val builder: StringBuilder = java.lang.StringBuilder()
        for (singlePlayer in YsmClientConnectionManager.getModInstalledPlayers()){
            builder.append(I18nManager.parseTranslatableKey("commands.listysmplayers.single_entry_format", arrayOf(singlePlayer.name,PlayerDataManager.createOrGetPlayerData(singlePlayer.name).mainResourceLocation)))
        }
        sender.sendMessage(builder.toString())
        return true
    }
}