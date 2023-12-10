package me.earthme.mysm.manager

import com.google.gson.GsonBuilder
import me.earthme.mysm.MyYSM
import me.earthme.mysm.data.PlayerModelData
import me.earthme.mysm.model.loaders.GlobalModelLoader
import me.earthme.mysm.utils.AsyncExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

object PlayerDataManager {
    private val ALL_LOADED_DATA: MutableMap<String, PlayerModelData> = ConcurrentHashMap()
    private val GSON = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private var PLAYER_DATA_FOLDER = File("playerdata")
    private val SAVE_SCHEDULER = CompletableFuture.delayedExecutor(1,TimeUnit.MINUTES, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE)
    @Volatile
    private var shouldSaveNext = true

    private fun checkIncorrect(player: Player): Boolean{
        /*val targetData = ALL_LOADED_DATA[player.name] ?: return false
        val allModels = GlobalModelLoader.getAllLoadedModelData()
        var containedIfNoAuthRequired = false

        for (singleModel in allModels){
            val needAuth = GlobalModelLoader.needModelAuth(singleModel.getModelName())

            if (needAuth && singleModel.getModelName() == targetData.mainResourceLocation.key && !ModelPermissionManager.isPlayerHeldModel(player,targetData.mainResourceLocation)){ //Because it equals the target model,So use the inited namespacedkey in the playerdata
                return true //Direct return if it passed the check above
            }else if(singleModel.getModelName() == targetData.mainResourceLocation.key && !needAuth){
                containedIfNoAuthRequired = true
                break //Break out the loop if it contained
            }
        }

        return !containedIfNoAuthRequired //If it passed the permission check and it also in the model list,Return true*/
        return false
    }

    fun setToDefaultIfIncorrect(player: Player){
        if (checkIncorrect(player)){
            val targetData = createOrGetPlayerData(player.name)
            targetData.mainResourceLocation = MyYSM.defaultModelLocation!!
            targetData.mainTextPngResourceLocation = MyYSM.defaultModelTextureLocation!!
            targetData.isDirty = true
        }
    }

    fun saveAllData(){
        val asyncTasks: MutableList<CompletableFuture<Void>> = ArrayList()
        for (playerEntry in ALL_LOADED_DATA.entries){
            val playerName = playerEntry.key
            val data = playerEntry.value

            if (data.isDirty){
                asyncTasks.add(CompletableFuture.runAsync({
                    val dataFile = File(PLAYER_DATA_FOLDER,playerName)
                    Files.writeString(dataFile.toPath(), GSON.toJson(data))
                    data.isDirty = false
                }, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE))
            }
        }

        for (asyncTask in asyncTasks){
            asyncTask.join()
        }
    }

    private fun scheduleAndSave(){
        SAVE_SCHEDULER.execute {
            try {
                saveAllData()
            }finally {
                if (shouldSaveNext) {
                    scheduleAndSave()
                }
            }
        }
    }

    fun createOrGetPlayerData(playerName: String): PlayerModelData {
        var ret = ALL_LOADED_DATA[playerName]

        if (ret == null){
            ret = createPlayerData(playerName)
        }

        return ret
    }

    fun loadAllDataFromFolder(plugin: Plugin){
        PLAYER_DATA_FOLDER = File(plugin.dataFolder,"playerdata")
        if (!PLAYER_DATA_FOLDER.mkdir()){
            val fileList = PLAYER_DATA_FOLDER.listFiles()
            fileList?.let {
                val stream = Arrays.stream(it)

                CompletableFuture.allOf(*stream.map { fileEntry ->
                    CompletableFuture.runAsync({
                        val data = Files.readAllBytes(fileEntry.toPath())
                        val entry = GSON.fromJson(String(data), PlayerModelData::class.java)
                        ALL_LOADED_DATA[entry.username] = entry
                    }, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE)
                }.toArray { len -> arrayOfNulls(len) }).join()

                plugin.logger.info("Loaded ${ALL_LOADED_DATA.size} player data!")
            }
        }

        plugin.logger.info("Save scheduler has started!")
        scheduleAndSave()
    }

    private fun createPlayerData(playerName: String): PlayerModelData {
        val ret = PlayerModelData(
                doAnimation = false,
                sendAnimation = true,
                currentAnimation = "idle",
                isDirty = true,
                mainResourceLocation = MyYSM.defaultModelLocation!!,
                mainTextPngResourceLocation = MyYSM.defaultModelTextureLocation!!,
                username = playerName)
        ALL_LOADED_DATA[playerName] = ret
        return ret
    }
}