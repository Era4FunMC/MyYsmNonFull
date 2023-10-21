package me.earthme.mysm.manager

import me.earthme.mysm.data.ModelPermissionData
import me.earthme.mysm.utils.AsyncExecutor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ModelPermissionManager {
    private var permissionData: ModelPermissionData = ModelPermissionData(
        ConcurrentHashMap.newKeySet(),
        ConcurrentHashMap.newKeySet(),
        ConcurrentHashMap.newKeySet()
    )
    private var permissionDataFile: File? = null
    private val saveScheduler = CompletableFuture.delayedExecutor(1,TimeUnit.MINUTES, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE)
    @Volatile
    private var shouldSaveNext: Boolean = true

    fun loadOrInitFromFile(pluginInstance: Plugin){
        this.permissionDataFile = File(pluginInstance.dataFolder,"permission_data.json")
        if (this.permissionDataFile!!.exists()){
            pluginInstance.logger.info("Reading exists permission data file")
            permissionData = ModelPermissionData.fromFile(this.permissionDataFile!!)
        }else{
            pluginInstance.logger.info("Creating new permission data file")
            permissionData.writeToFile(this.permissionDataFile!!)
        }

        this.saveScheduler.execute { this.scheduleThenSave() }
    }

    fun isPlayerHeldModel(player: Player,modelLocation: NamespacedKey): Boolean{
        return this.permissionData.doesPlayerHeldModel(modelLocation,player)
    }

    fun addPlayerHeldModel(player: Player,modelLocation: NamespacedKey){
        this.permissionData.addPlayerHeldModel(modelLocation,player)
    }

    fun close(){
        this.save()
        this.shouldSaveNext = false
    }

    private fun save(){
        this.permissionDataFile?.let{
            if (!it.exists()){
                return@let
            }

            this.permissionData.writeToFile(it)
            this.permissionData.isDirty = false
        }
    }

    private fun scheduleThenSave(){
        if (!this.shouldSaveNext){
            return
        }

        try {
            this.save()
        }finally {
            saveScheduler.execute {
                scheduleThenSave()
            }
        }
    }

    fun isModelNeedAuth(modelLocation: NamespacedKey): Boolean{
        return this.permissionData.isModelNeedAuth(modelLocation)
    }

    fun setModelNeedAuth(modelLocation: NamespacedKey,needAuth: Boolean){
        this.permissionData.setModelNeedAuth(modelLocation,needAuth)
    }

    fun getHeldModelsOfPlayer(player: Player): Set<NamespacedKey>{
        return this.permissionData.getAllHeldDataOfPlayer(player)
    }

    fun removePlayerHeldModel(player: Player,modelLocation: NamespacedKey){
        this.permissionData.removePlayerHeldModel(modelLocation,player)
    }
}