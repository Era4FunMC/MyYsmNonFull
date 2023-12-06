package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.network.YsmClientConnectionManager
import me.earthme.mysm.utils.MessageBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ListPlayersCommand : AbstractCommand("listysmplayers") {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        // 无参数
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdListYsmPlayers)){
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        for (singlePlayer in YsmClientConnectionManager.getModInstalledPlayers()){
            mb
                .translatable("commands.listysmplayers.single_entry_format", arrayOf(singlePlayer.name,PlayerDataManager.createOrGetPlayerData(singlePlayer.name).mainResourceLocation))
                .newLine()
        }
        sender.sendMessage(mb.toComponent())
        return true
    }
}