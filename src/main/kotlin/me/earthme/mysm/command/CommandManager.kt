package me.earthme.mysm.command

import org.bukkit.Bukkit

class CommandManager {
    fun init() {

    }

    fun register(impl: AbstractCommand) {
        val cmd = Bukkit.getPluginCommand(impl.name) ?: throw NullPointerException("Unknown command ${impl.name}")

        cmd.setExecutor(impl)
        cmd.tabCompleter = impl
        cmd.aliases = impl.aliases
        cmd.description = impl.desc
    }
}