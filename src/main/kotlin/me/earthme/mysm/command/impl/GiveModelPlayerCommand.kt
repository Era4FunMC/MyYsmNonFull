package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.utils.MessageBuilder
import me.earthme.mysm.utils.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class GiveModelPlayerCommand : AbstractCommand("gmodeltp") {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val lst = mutableListOf<String>()

        if (args.size == 1){
            // 参数0：返回玩家名称
            lst.addAll(onlinePlayerNames(args[0]))
        } else if (args.size == 2) {
            // 参数1：返回模型位置
            lst.addAll(modelLocations(args[1]))
        }

        return lst
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdGModelTp)){
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        if (args.size != 2){
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val targetModel = NamespacedKey.fromString(args[1]) ?: return false

        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(mb.translatable("commands.global.target_player_not_found").toComponent())
            return true
        }

        if (!ModelPermissionManager.isModelNeedAuth(targetModel)){
            sender.sendMessage(mb.translatable("commands.global.target_model_does_not_require_authentic").toComponent())
            return true
        }

        MiscUtils.giveModelToPlayer(targetPlayer,targetModel)
        sender.sendMessage(mb.translatable("commands.gmodelfp.successfully_executed").toComponent())
        return true
    }
}