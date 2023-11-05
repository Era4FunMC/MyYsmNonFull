package me.earthme.mysm

import com.github.retrooper.packetevents.PacketEvents
import me.earthme.mysm.commands.*
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.model.loaders.VersionedCacheLoader
import me.earthme.mysm.network.YsmClientConnectionManager
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

/**
 * 用于初始化整个插件的类()
 */
object PluginBootstrap {
    fun initAll(pluginInstance: Plugin){
        ResourceConstants.initAll(pluginInstance)
        ModelPermissionManager.loadOrInitFromFile(pluginInstance)
        VersionedCacheLoader.init(pluginInstance)
        GlobalModelLoader.init(pluginInstance)
        PlayerDataManager.loadAllDataFromFolder(pluginInstance)
        YsmClientConnectionManager.init(pluginInstance)

        Bukkit.getPluginManager().registerEvents(YsmClientConnectionManager,pluginInstance)
        PacketEvents.getAPI().eventManager.registerListener(YsmClientConnectionManager)
        pluginInstance.logger.info("Registed packet and event listener")

        pluginInstance.logger.info("Starting handler tick loop")
        YsmClientConnectionManager.tickThenSchedule() //Tick once to start the tickloop

        pluginInstance.logger.info("Registering commands")
        Bukkit.getPluginCommand("gmodeltp")!!.setExecutor(GiveModelPlayerCommand())
        Bukkit.getPluginCommand("smodelna")!!.setExecutor(SetModelNeedAuthCommand())
        Bukkit.getPluginCommand("reloadmodels")!!.setExecutor(ReloadModelsCommand())
        Bukkit.getPluginCommand("listysmplayers")!!.setExecutor(ListPlayersCommand())
        Bukkit.getPluginCommand("dmodelfp")!!.setExecutor(DropModelPlayerCommand())
        Bukkit.getPluginCommand("playanimationonplayer")!!.setExecutor(PlayAnimationCommand())
        pluginInstance.logger.info("Registed commands")
    }

    fun unloadAll(pluginInstance: Plugin){
        pluginInstance.logger.info("Closing all managers")
        PlayerDataManager.saveAllData()
        ModelPermissionManager.close()
        pluginInstance.logger.info("Terminating packet handler")
        YsmClientConnectionManager.awaitShutdown()
    }
}