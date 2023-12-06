package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.MessageBuilder
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetModelNeedAuthCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.modelManagement)){
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        if (args == null || args.size != 2){
            return false
        }

        val targetModel = NamespacedKey.fromString(args[0]) ?: return false
        val needAuth = if (args[1] == "true"){ true } else if (args[1] == "false"){ false } else { return false }

        if (!VersionedCacheLoader.hasLoadedModel(targetModel.key)){
            sender.sendMessage(mb.translatable("commands.global.target_model_not_found").toComponent())
            return true
        }

        if (needAuth && ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(mb.translatable("commands.smodelna.target_model_already_in_auth_list").toComponent())
            return true
        }

        if (!needAuth && !ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(mb.translatable("commands.smodelna.target_model_already_out_of_auth_list").toComponent())
            return true
        }

        sender.sendMessage(mb.translatable("commands.smodelna.successfully_executed").toComponent())
        MiscUtils.setModelNeedAuth(targetModel,needAuth)
        return true
    }
}