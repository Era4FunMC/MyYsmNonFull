package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.utils.message.MessageBuilder
import me.earthme.mysm.utils.api.MiscUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class SetPlayerModelCommand : AbstractCommand("smodelfp"){
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val lst = mutableListOf<String>()

        if (args.size == 1) {
            // 参数0：玩家名称
            lst.addAll(onlinePlayerNames(args[0]))
        } else if (args.size == 2) {
            // 参数1：模型名称
            lst.addAll(modelLocations(args[1]))
        }

        return lst
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdSModelFp)){
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        if (args.size != 2){
            return false
        }

        val targetModelLocation = NamespacedKey.fromString(args[1]) ?: return false
        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null || !targetPlayer.isOnline){
            sender.sendMessage(mb.translatable("commands.global.target_player_not_found").toComponent())
            return true
        }

        if (!VersionedCacheLoader.hasLoadedModel(targetModelLocation.key)){
            sender.sendMessage(mb.translatable("commands.global.target_model_not_found").toComponent())
            return true
        }

        MiscUtils.setModelForPlayer(targetPlayer,targetModelLocation)

        return true
    }
}