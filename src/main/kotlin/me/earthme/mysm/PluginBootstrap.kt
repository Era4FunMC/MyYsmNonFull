package me.earthme.mysm

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import me.earthme.mysm.commands.*
import me.earthme.mysm.connection.FabricPlayerYsmConnection
import me.earthme.mysm.connection.ForgePlayerYsmConnection
import me.earthme.mysm.manager.PlayerDataManager
import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.manager.MultiSupportedVersionCacheManager
import me.earthme.mysm.network.MainYsmNetworkHandler
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object PluginBootstrap {

    fun initAllManager(pluginInstance: Plugin){
        ResourceConstants.initAll(pluginInstance)
        ModelPermissionManager.loadOrInitFromFile(pluginInstance)
        MultiSupportedVersionCacheManager.init(pluginInstance)
        PlayerDataManager.loadAllDataFromFolder(pluginInstance)
        MainYsmNetworkHandler.init(pluginInstance)

        Bukkit.getPluginManager().registerEvents(MainYsmNetworkHandler,pluginInstance)
        PacketEvents.getAPI().eventManager.registerListener(MainYsmNetworkHandler)
        pluginInstance.logger.info("Registed packet and event listener")

        pluginInstance.logger.info("Starting handler tick loop")
        MainYsmNetworkHandler.tickThenSchedule() //Tick once to start the tickloop

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
        MainYsmNetworkHandler.awaitShutdown()
    }
}