package me.earthme.mysm.command

import me.earthme.mysm.command.impl.*
import org.bukkit.Bukkit

class CommandManager {
    fun init() {
        register(DropModelPlayerCommand())
        register(GiveModelPlayerCommand())
        register(ListPlayersCommand())
        register(PlayAnimationCommand())
        register(ReloadModelsCommand())
        register(SetModelNeedAuthCommand())
        register(SetPlayerModelCommand())
        register(OpenManagementGUICommand())
    }

    fun register(impl: AbstractCommand) {
        val cmd = Bukkit.getPluginCommand(impl.name) ?: throw NullPointerException("Unknown command ${impl.name}")

        cmd.setExecutor(impl)
        cmd.tabCompleter = impl
        if (impl.aliases.isNotEmpty()) cmd.aliases = impl.aliases
        if (impl.desc.isNotEmpty()) cmd.description = impl.desc
    }
}