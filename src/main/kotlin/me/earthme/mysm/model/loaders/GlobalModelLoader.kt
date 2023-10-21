package me.earthme.mysm.model.loaders

import me.earthme.mysm.manager.ModelPermissionManager
import me.earthme.mysm.model.IModelLoader
import me.earthme.mysm.model.YsmModelData
import me.earthme.mysm.model.loaders.impl.FolderYsmModelLoader
import me.earthme.mysm.model.loaders.impl.ZipFileYsmLoaderImpl
import me.earthme.mysm.utils.AsyncExecutor
import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.logging.Level

object GlobalModelLoader {
    private val allLoaderImpls : MutableSet<IModelLoader> = ConcurrentHashMap.newKeySet()
    private val loadedYsmModels: MutableMap<String, YsmModelData> = ConcurrentHashMap()
    private var pluginInstance: Plugin? = null
    private var modelDir: File = File("models")

    fun init(pluginInstance: Plugin){
        this.pluginInstance = pluginInstance
        this.modelDir = File(pluginInstance.dataFolder,"ysm_models")
        this.addDefaultLoaders()
        this.loadAllModels()
        this.writeAllModelsToCache()
    }

    fun addLoader(modelLoader: IModelLoader){
        this.allLoaderImpls.add(modelLoader)
    }

    private fun addDefaultLoaders(){
        this.allLoaderImpls.add(FolderYsmModelLoader())
        this.allLoaderImpls.add(ZipFileYsmLoaderImpl())
    }

    private fun writeAllModelsToCache(){
        CompletableFuture.allOf(
            *this.loadedYsmModels.values.stream()
                .map { modelData -> CompletableFuture.runAsync({
                    VersionedCacheLoader.writeCacheForModel(modelData)
                }, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE) }
                .toArray { i -> arrayOfNulls(i) }
        ).join()

    }

    fun reloadAll(){
        this.dropAllModels()
        this.loadAllModels()
        this.writeAllModelsToCache()
    }

    private fun dropAllModels(){
        this.loadedYsmModels.clear()
    }

    private fun loadAllModels(){
        if (this.modelDir.mkdir()){
            return
        }

        this.modelDir.listFiles()?.let{ files ->
            CompletableFuture.allOf(*Arrays.stream(files)
                .map { file -> CompletableFuture.runAsync({
                     try {
                         this.loadSingleModel(file)
                     }catch (e: Exception){
                         this.pluginInstance!!.logger.log(Level.SEVERE,"Failed to load model file ${file.name}!",e)
                     }
                },AsyncExecutor.ASYNC_EXECUTOR_INSTANCE) }
                .toArray{ arrayOfNulls(it) }).join()
        }
        this.pluginInstance!!.logger.info("Loaded ${this.loadedYsmModels.size} models!")
    }


    fun getTargetModelData(modelName: String): YsmModelData?{
        return this.loadedYsmModels[modelName]
    }

    private fun loadSingleModel(file: File){
        val targetModelLoader = this.searchForAMatchedLoader(file)

        if (targetModelLoader == null){
            this.pluginInstance!!.logger.warning("No target loader matched for file ${file.name}!")
            return
        }

        val loaded = targetModelLoader.loadModel(file, Function { modelName -> this.needModelAuth(modelName) })
        this.loadedYsmModels[loaded.getModelName()] = loaded

        this.pluginInstance!!.logger.info("Loaded model ${loaded.getModelName()}")
    }

    private fun searchForAMatchedLoader(file: File): IModelLoader?{
        for (singleLoader in this.allLoaderImpls){
            if (singleLoader.canLoad(file)){
                return singleLoader
            }
        }

        return null
    }

    private fun needModelAuth(modelName: String): Boolean{
        return ModelPermissionManager.isModelNeedAuth(NamespacedKey("yes_steve_model", modelName))
    }
}