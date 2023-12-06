package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetPlayerModelCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.commands.smodelfp") && !sender.isOp){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.no_permission"))
            return true
        }

        if (args!!.size != 2){
            return false
        }

        val targetModelLocation = NamespacedKey.fromString(args[1]) ?: return false
        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_player_not_found"))
            return true
        }

        if (!VersionedCacheLoader.hasLoadedModel(targetModelLocation.key)){
            sender.sendMessage(I18nManager.parseTranslatableKey("commands.global.target_model_not_found"))
            return true
        }

        MiscUtils.setModelForPlayer(targetPlayer,targetModelLocation)

        return true
    }
}