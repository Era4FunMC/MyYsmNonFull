package me.earthme.mysm.commands

import me.earthme.mysm.I18nManager
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadModelsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.reload") && !sender.isOp){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        MiscUtils.reloadAllModels()
        sender.sendMessage(I18nManager.parseTranslatableKey("commands.reloadmodels.successfully_executed"))
        return true
    }
}