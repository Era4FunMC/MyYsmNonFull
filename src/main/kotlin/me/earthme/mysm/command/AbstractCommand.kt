package me.earthme.mysm.command

import me.earthme.mysm.model.loaders.GlobalModelLoader
import org.bukkit.Bukkit
import org.bukkit.command.TabExecutor

abstract class AbstractCommand(val name: String, val desc: String = "", val aliases: List<String> = emptyList()): TabExecutor {
    /**
     * 返回在线玩家名称，用于TabComplete
     */
    protected fun onlinePlayerNames(): MutableList<String> {
        val lst = mutableListOf<String>()
        for (p in Bukkit.getOnlinePlayers()) lst.add(p.name)
        return lst
    }

    /**
     * 返回匹配的玩家名称
     */
    protected fun onlinePlayerNames(name: String): MutableList<String> {
        val lst = mutableListOf<String>()
        for (n in onlinePlayerNames()) if (n.startsWith(name)) lst.add(n)
        return lst
    }

    /**
     * true/false 选项时匹配
     */
    protected fun trueOrFalse(s: String): List<String> {
        return if ("true".startsWith(s)) {
            listOf("true")
        } else if ("false".startsWith(s)) {
            listOf("false")
        } else {
            listOf("true, false")
        }
    }

    /**
     * 模型名称
     */
    protected fun modelLocations(s: String): List<String> {
        val lst = mutableListOf<String>()

        for (m in GlobalModelLoader.getAllLoadedModels()) if (m.startsWith(s)) lst.add(m)

        return lst
    }
}