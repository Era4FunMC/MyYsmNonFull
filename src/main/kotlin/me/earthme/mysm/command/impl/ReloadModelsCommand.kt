package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.utils.MessageBuilder
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ReloadModelsCommand : AbstractCommand("reloadmodels") {
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

        if (!sender.hasPermission(PermissionConstants.cmdReload)){
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        MiscUtils.reloadAllModels()
        sender.sendMessage(mb.translatable("commands.reloadmodels.successfully_executed").toComponent())
        return true
    }
}