package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetModelNeedAuthCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission(PermissionConstants.modelManagement)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        if (args == null || args.size != 2){
            return false
        }

        val targetModel = NamespacedKey.fromString(args[0]) ?: return false
        val needAuth = if (args[1] == "true"){ true } else if (args[1] == "false"){ false } else { return false }

        if (!VersionedCacheLoader.hasLoadedModel(targetModel.key)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_model_not_found"))
            return true
        }

        if (needAuth && ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.smodelna.target_model_already_in_auth_list"))
            return true
        }

        if (!needAuth && !ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.smodelna.target_model_already_out_of_auth_list"))
            return true
        }

        sender.sendMessage(I18nManager.parseTranslatableKey("commands.smodelna.successfully_executed"))
        MiscUtils.setModelNeedAuth(targetModel,needAuth)
        return true
    }
}