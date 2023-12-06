package me.earthme.mysm.command

import org.bukkit.command.Command
import org.bukkit.command.TabExecutor

abstract class AbstractCommand(val name: String, val desc: String = "", val usage: String = "/$name", val aliases: List<String> = emptyList()): TabExecutor {

}