package me.earthme.mysm.command.impl

import me.earthme.mysm.command.AbstractCommand
import me.earthme.mysm.utils.api.MiscUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpenManagementGUICommand : AbstractCommand("managementmodels") {
    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, command: Command, p2: String, args: Array<out String>?): Boolean {
        if (!sender.hasPermission("myysm.model.management")){
            return true
        }

        if (sender !is Player){
            return false
        }

        MiscUtils.openManagementGUIToPlayer(sender)

        return true
    }
}