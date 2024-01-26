package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.utils.message.MessageBuilder
import me.earthme.mysm.utils.api.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class DropModelPlayerCommand: AbstractCommand("dmodelfp") {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val lst = mutableListOf<String>()

        if (args.size == 1){
            // 参数0：返回匹配玩家名称
            lst.addAll(onlinePlayerNames(args[0]))
        } else if (args.size == 2) {
            // 参数1：返回此玩家模型列表
            val player = Bukkit.getPlayer(args[0])
            if (player != null) {
                // 不存在则不执行
                for (d in ModelPermissionManager.getPlayerModelList(player).map { it.key }) lst.add(d.toString())
            }
        }

        return lst
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdDMotelFp)){
            mb.translatable("commands.global.no_permission")
            sender.sendMessage(mb.toComponent())
            return true
        }

        if (args.size != 2){
            return false
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        val targetModel = NamespacedKey.fromString("yes_steve_model:" + args[1]) ?: return false

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

        mb.translatable("commands.dmodelfp.successfully_executed", targetModel.key,targetPlayer.name)
        sender.sendMessage(mb.toComponent())
        return true
    }
}