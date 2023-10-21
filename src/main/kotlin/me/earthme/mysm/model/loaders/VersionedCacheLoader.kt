package me.earthme.mysm.model.loaders

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import me.earthme.mysm.data.mod.YsmVersionMeta
import me.earthme.mysm.data.mod.YsmVersionMetaArray
import me.earthme.mysm.model.YsmModelData
import me.earthme.mysm.model.cache.CacheAESKeyFile
import me.earthme.mysm.model.cache.WrappedCacheData
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.utils.FileUtils
import me.earthme.mysm.utils.HttpsUtils
import me.earthme.mysm.utils.ysm.MD5Utils
import org.bukkit.plugin.Plugin
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.logging.Level

@OptIn(InternalCoroutinesApi::class)
object VersionedCacheLoader {
    private val versionMetaMap: MutableSet<YsmVersionMeta> = ConcurrentHashMap.newKeySet()
    private val loadCacheClassLoaders: MutableMap<YsmVersionMeta,URLClassLoader> = ConcurrentHashMap()

    private val modelToVersion2Caches: MutableMap<String,MutableMap<YsmVersionMeta,String>> = HashMap()

    private var baseCacheDir: File = File("caches")
    private var modJarFolder: File = File("modjars")

    private var passwordFile: File = File("password.ysmdata")
    private var passwordFileInstance: CacheAESKeyFile = CacheAESKeyFile.random
    private var pluginInstance: Plugin? = null
    private var metaArray: YsmVersionMetaArray? = null

    fun reload(){
        dropAllCaches()
        dropAll()
    }

    private fun dropAll(){
        this.loadCacheClassLoaders.clear()
        synchronized(this.modelToVersion2Caches){
            this.modelToVersion2Caches.clear()
        }
    }

    fun refreshCache(modelData: YsmModelData){
        val needToRemove: MutableList<File> = ArrayList()

        synchronized(this.modelToVersion2Caches){
            if (this.modelToVersion2Caches.containsKey(modelData.getModelName())){
                for ((version,fileName) in this.modelToVersion2Caches[modelData.getModelName()]!!){
                    val targetFolder = File(baseCacheDir,"version_${version.version}_${version.modLoader}")
                    val targetFile = File(targetFolder,fileName)

                    needToRemove.add(targetFile)
                }

                this.modelToVersion2Caches.remove(modelData.getModelName())
            }
        }

        for (file in needToRemove){
            file.delete()
        }

        for (singleVersion in this.versionMetaMap){
            writeToCache(modelData,singleVersion)
        }
    }

    fun getVersionMeta(modLoader: String,protocolId: Int): YsmVersionMeta? {
        for (singleMeta in this.versionMetaMap){
            if (singleMeta.version == protocolId && singleMeta.modLoader == modLoader){
                return singleMeta
            }
        }

        return null
    }

    fun getCachesWithoutMd5Contained(md5Excludes: List<String>, actionIfNotContained: Consumer<ByteArray>, actionIfContained: Consumer<String>, version: YsmVersionMeta){
        synchronized(this.modelToVersion2Caches){
            for (entry in this.modelToVersion2Caches){
                AsyncExecutor.ASYNC_EXECUTOR_INSTANCE.execute{
                    val version2Model = entry.value
                    val md5WithFileName = version2Model[version]!!
                    if (md5Excludes.contains(md5WithFileName)){
                        actionIfContained.accept(md5WithFileName)
                        return@execute
                    }

                    val targetFolder = File(baseCacheDir,"version_${version.version}_${version.modLoader}")
                    val targetFile = File(targetFolder,md5WithFileName)
                    actionIfNotContained.accept(Files.readAllBytes(targetFile.toPath()))
                }
            }
        }
    }

    /*private fun writeAllModelsToCache(){
        if (loadedYsmModels.isEmpty()){
            return
        }

        CompletableFuture.allOf(
            *loadedYsmModels.keys.stream()
                .map { name -> CompletableFuture.runAsync({
                    try {
                        for (singleVersionMeta in versionMetaMap){
                            writeToCache(name,singleVersionMeta)
                        }
                    }catch (e: Exception){
                        pluginInstance!!.logger.log(Level.SEVERE,"Error while loading model ${name}!",e)
                    }
                }, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE) }
                .toArray { i -> arrayOfNulls(i) }
        ).join()

        synchronized(modelToVersion2Caches) {
            pluginInstance!!.logger.info("Loaded ${modelToVersion2Caches.size} caches!")
        }
    }*/

    fun writeCacheForModel(modelData: YsmModelData){
        for (singleVersionMeta in versionMetaMap){
            this.writeToCache(modelData,singleVersionMeta)
        }
    }

    fun getPasswordData(): ByteArray{
        return this.passwordFileInstance.data
    }

    private fun writeToCache(modelData: YsmModelData, version: YsmVersionMeta){
        val dataClassLoader: URLClassLoader = getCacheDataClassLoaderForVersion(version)!!
        val modelFileInstance: WrappedCacheData = WrappedCacheData.createFromModelData(
            modelData,
            dataClassLoader.loadClass(version.dataClassName),
            dataClassLoader as ClassLoader
        )

        val cacheData = modelFileInstance.toWritableBytes(modelFileInstance, this.passwordFileInstance.secretKey,
            this.passwordFileInstance.algorithmParameterSpec)
        val fileName = MD5Utils.getMd5(cacheData)
        val targetFolder = File(this.baseCacheDir,"version_${version.version}_${version.modLoader}")
        targetFolder.mkdirs()
        val targetCacheFile = File(targetFolder,fileName)
        Files.write(targetCacheFile.toPath(),cacheData)

        synchronized(this.modelToVersion2Caches){
            if (!this.modelToVersion2Caches.containsKey(modelFileInstance.getModelName())) {
                this.modelToVersion2Caches[modelFileInstance.getModelName()] = ConcurrentHashMap()
            }

            this.modelToVersion2Caches[modelFileInstance.getModelName()]!![version] = fileName
        }
    }

    private fun getCacheDataClassLoaderForVersion(version: YsmVersionMeta): URLClassLoader?{
        return this.loadCacheClassLoaders[version]
    }

    private fun loadVersionMeta(){
        val metaArrayInputStream = VersionedCacheLoader::class.java.classLoader.getResourceAsStream("ysm_data/ysm_version_meta.json")
        val loadData = FileUtils.readInputStreamToByte(metaArrayInputStream!!)
        this.metaArray = YsmVersionMetaArray.readFromJson(String(loadData!!))
    }

    private fun downloadModJars(){
        modJarFolder.mkdir()
        val asyncTasks: MutableList<CompletableFuture<Int>> = ArrayList()
        for (singleMeta in this.metaArray!!.versionMetas){
            val targetFile = File(modJarFolder,"modjar_"+singleMeta.version+"_"+singleMeta.modLoader+".jar")

            if (!targetFile.exists()){
                this.pluginInstance!!.logger.info("Mod jar ${targetFile.name} has not found!Downloading from curse forge")
                asyncTasks.add(CompletableFuture.supplyAsync({
                    try {
                        val downloaded = HttpsUtils.downloadFrom(singleMeta.downloadLink)!!
                        Files.write(targetFile.toPath(),downloaded)

                        this.loadCacheClassLoaders[singleMeta] = URLClassLoader(arrayOf(targetFile.toURI().toURL()), VersionedCacheLoader::class.java.classLoader)
                        this.versionMetaMap.add(singleMeta)
                    }catch (e: Exception){
                        this.pluginInstance!!.logger.log(Level.SEVERE,"Error while downloading jar file!",e)
                    }

                    return@supplyAsync singleMeta.version
                },AsyncExecutor.ASYNC_EXECUTOR_INSTANCE))
            }else{
                this.pluginInstance!!.logger.info("Loading exists jar ${targetFile.name}")
                this.loadCacheClassLoaders[singleMeta] = URLClassLoader(arrayOf(targetFile.toURI().toURL()), VersionedCacheLoader::class.java.classLoader)
                this.versionMetaMap.add(singleMeta)
            }
        }

        for (singleTask in asyncTasks){
            try {
                this.pluginInstance!!.logger.info("Loaded jar for mc version ${singleTask.join()}")
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    /*private fun loadAllModels(){
        modelDir.listFiles()?.let{
            CompletableFuture.allOf(
                *Arrays.stream(it)
                    .map { file -> CompletableFuture.runAsync({
                        try {
                            val fileName = file.name
                            val authChecker: Function<String,Boolean> = Function { return@Function needModelAuth(fileName) }

                            if (file.isDirectory){
                                pluginInstance!!.logger.info("Loading model $fileName as a directory")
                                val files = Arrays.stream(file.listFiles()!!).collect(Collectors.toList())
                                val ysmModelData = YsmModelData.createFromFolder(files,fileName,authChecker)
                                loadedYsmModels[ysmModelData.getModelName()] = ysmModelData
                            }else{
                                /*this.pluginInstance!!.logger.info("Loading model $fileName as a file")
                                if (fileName.endsWith(".ysm")){
                                    val ysmModelData = YsmModelData.createFromYsmFile(file,authChecker)
                                    this.loadedYsmModelData[ysmModelData.getModelName()] = ysmModelData
                                }*/
                            }
                        }catch (e: Exception){
                            pluginInstance!!.logger.log(Level.SEVERE,"Error while loading model ${file.name}!",e)
                        }
                    }, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE) }
                    .toArray { i -> arrayOfNulls(i) }
            ).join()
            pluginInstance!!.logger.info("Loaded ${loadedYsmModels.size} model data!")
        }
    }*/

    private fun dropAllCaches(){
        FileUtils.forEachFolder(this.baseCacheDir){
            try {
                if (!it.isDirectory){
                    it.delete()
                }
            }catch (e : Exception){
                this. pluginInstance!!.logger.log(Level.SEVERE,"Error while deleting cache file ${it.name}!",e)
            }
        }
    }

    private fun loadPasswordFile(){
        if (this.passwordFile.exists()){
            this.pluginInstance!!.logger.info("Loading exists password file")
            this.passwordFileInstance = CacheAESKeyFile.readFromFile(this.passwordFile)!!
        }else{
            this.pluginInstance!!.logger.info("Creating password file")
            Files.write(this.passwordFile.toPath(), this.passwordFileInstance.encodeToByte())
        }
    }

    private fun initVars(){
        this.baseCacheDir = File(this.pluginInstance!!.dataFolder,"ysm_caches")
        this.passwordFile = File(this.pluginInstance!!.dataFolder,"password.ysmdata")
        this.modJarFolder = File(this.pluginInstance!!.dataFolder,"mod_jars")

    }

    fun init(pluginInstance: Plugin){
        this.pluginInstance = pluginInstance

        this.initVars()
        this.loadPasswordFile()
        this.dropAllCaches()
        this.loadVersionMeta()
        this.downloadModJars()
    }
}