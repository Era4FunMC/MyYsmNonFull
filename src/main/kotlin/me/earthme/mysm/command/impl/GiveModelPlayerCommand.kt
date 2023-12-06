package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GiveModelPlayerCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.gmodeltp") && !sender.isOp){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        if (args == null || args.size != 2){
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val targetModel = NamespacedKey.fromString(args[1]) ?: return false

        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_player_not_found"))
            return true
        }

        if (!ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_model_does_not_require_authentic"))
            return true
        }

        MiscUtils.giveModelToPlayer(targetPlayer,targetModel)
        sender.sendMessage(I18nManager.parseTranslatableKey("commands.gmodelfp.successfully_executed"))
        return true
    }
}