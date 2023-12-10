package me.earthme.mysm.command.impl

import me.earthme.mysm.PermissionConstants
import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.utils.message.MessageBuilder
import me.earthme.mysm.utils.api.MiscUtils
import me.earthme.mysm.utils.YsmModelUtils
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PlayAnimationCommand: AbstractCommand("playanimationonplayer") {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        val lst = mutableListOf<String>()

        if (args.size == 1) {
            // 参数0：返回符合玩家名称列表
            lst.addAll(onlinePlayerNames(args[0]))
        } else if (args.size == 2) {
            // 参数1：返回此玩家动画列表
            val player = Bukkit.getPlayer(args[1])

            // 不存在或不在线则不执行
            if (player != null && player.isOnline) {
                for (n in getAnimationNames(player)) if (n.startsWith(args[1])) lst.add(n)
            }
        }

        return lst
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var mb = MessageBuilder()

        if (!sender.hasPermission(PermissionConstants.cmdPlayAnimationOnPlayer)) {
            sender.sendMessage(mb.translatable("commands.global.no_permission").toComponent())
            return true
        }

        if (args.size == 2){
            val targetPlayerName = args[0]
            val targetAnimationName = args[1]

            val targetPlayer = Bukkit.getPlayer(targetPlayerName)

            if (targetPlayer == null){
                sender.sendMessage(mb.translatable("commands.global.target_player_not_found").toComponent())
                return true
            }

            val allAnimations = getAnimationNames(targetPlayer)

            if (!allAnimations.contains(targetAnimationName)){
                sender.sendMessage(mb.translatable("commands.global.target_animation_not_found").toComponent())

                mb = MessageBuilder()

                mb.translatable("commands.playanimationonplayer.animation_list_header").newLine()
                for (animationStr in allAnimations){
                    mb.translatable("commands.playanimationonplayer.single_entry_format",
                        arrayOf(animationStr)
                    ).newLine()
                }
                sender.sendMessage(mb.toComponent())
                return true
            }

            MiscUtils.playAnimationOnPlayer(targetPlayer,targetAnimationName)
            return true
        }

        return false
    }

    private fun getAnimationNames(player: Player): List<String> {
        val currentPlayerData = PlayerDataManager.createOrGetPlayerData(player.name)

        val currentModel = GlobalModelLoader.getTargetModelData(currentPlayerData.mainResourceLocation.key)!!
        return YsmModelUtils.getAnimationListFromModel(currentModel)
    }
}