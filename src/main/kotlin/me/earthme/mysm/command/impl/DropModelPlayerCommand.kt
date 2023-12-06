package me.earthme.mysm.command.impl

import me.earthme.mysm.I18nManager
import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.utils.MessageBuilder
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class DropModelPlayerCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdDMotelFp)){
            mb.translatable("commands.global.no_permission")
            sender.sendMessage(mb.toComponent())
            return true
        }

        if (args == null || args.size != 2){
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val targetModel = NamespacedKey.fromString(args[1]) ?: return false

        if (targetPlayer == null || !targetPlayer.isOnline){
            mb.translatable("commands.global.target_player_not_found")
            sender.sendMessage(mb.toComponent())
            return true
        }

        if (!ModelPermissionManager.isModelNeedAuth(targetModel)){
            mb.translatable("commands.global.target_model_does_not_require_authentic")
            sender.sendMessage(mb.toComponent())
            return true
        }

        MiscUtils.dropModelForPlayer(targetPlayer,targetModel)

        mb.translatable("commands.dmodelfp.successfully_executed")
        sender.sendMessage(mb.toComponent())
        return true
    }
}