package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.utils.MessageBuilder
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadModelsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
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