package me.earthme.mysm

import org.bukkit.Bukkit
import org.bukkit.permissions.Permission

object PermissionConstants {
    lateinit var cmdSModelFp: Permission private set
    lateinit var cmdPlayAnimationOnPlayer: Permission private set
    lateinit var cmdDMotelFp: Permission private set
    lateinit var cmdGModelTp: Permission private set
    lateinit var cmdReload: Permission private set
    lateinit var cmdListYsmPlayers: Permission private set
    lateinit var modelManagement: Permission private set

    fun init() {
        cmdSModelFp = getPerm("commands.smodelfp")
        cmdPlayAnimationOnPlayer = getPerm("commands.playanimationonplayer")
        cmdDMotelFp = getPerm("commands.dmodelfp")
        cmdGModelTp = getPerm("commands.gmodeltp")
        cmdReload = getPerm("commands.reload")
        cmdListYsmPlayers = getPerm("commands.listysmplayers")
        modelManagement = getPerm("model.management")
    }

    /**
     * 从myysm名称下获取权限实例
     */
    private fun getPerm(name: String) = Bukkit.getPluginManager().getPermission("myysm.$name") ?: throw NullPointerException("Unknown permission $name")
}