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
    private val md5ToCacheData: MutableMap<String,ByteArray> = HashMap()

    private var modJarFolder: File = File("modjars")

    private var passwordFile: File = File("password.ysmdata")
    private var passwordFileInstance: CacheAESKeyFile = CacheAESKeyFile.random
    private var pluginInstance: Plugin? = null
    private var metaArray: YsmVersionMetaArray? = null

    fun hasLoadedModel(modelName: String): Boolean{
        return this.modelToVersion2Caches.containsKey(modelName)
    }

    fun dropAll(){
        this.dropAllLoadedCaches()
    }

    private fun dropAllLoadedCaches(){
        synchronized(this.modelToVersion2Caches){
            this.modelToVersion2Caches.clear()
        }

        synchronized(this.md5ToCacheData){
            this.md5ToCacheData.clear()
        }
    }

    fun refreshCache(modelData: YsmModelData){
        synchronized(this.modelToVersion2Caches){
            if (this.modelToVersion2Caches.containsKey(modelData.getModelName())){
                for (md5 in this.modelToVersion2Caches[modelData.getModelName()]!!.values){
                    synchronized(this.md5ToCacheData){
                        this.md5ToCacheData.remove(md5)
                    }
                }
                this.modelToVersion2Caches.remove(modelData.getModelName())
            }
        }

        for (singleVersion in this.versionMetaMap){
            this.pushToCacheList(modelData,singleVersion)
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
                    val cacheMD5Key = version2Model[version]!!
                    if (md5Excludes.contains(cacheMD5Key)){
                        actionIfContained.accept(cacheMD5Key)
                        return@execute
                    }

                    val got: ByteArray = synchronized(this.md5ToCacheData){
                        this.md5ToCacheData[cacheMD5Key]!!
                    }

                    actionIfNotContained.accept(got)
                }
            }
        }
    }

    fun pushCacheForModel(modelData: YsmModelData){
        for (singleVersionMeta in versionMetaMap){
            this.pushToCacheList(modelData,singleVersionMeta)
        }
    }

    fun pushCacheForModelAsync(modelData: YsmModelData): CompletableFuture<*>{
        val allTasks: MutableList<CompletableFuture<*>> = ArrayList(this.versionMetaMap.size)

        for (singleVersionMeta in this.versionMetaMap){
            allTasks.add(CompletableFuture.runAsync({
                this.pushToCacheList(modelData,singleVersionMeta)
            },AsyncExecutor.ASYNC_EXECUTOR_INSTANCE))
        }

        return CompletableFuture.allOf(*allTasks.stream().toArray { size -> arrayOfNulls<CompletableFuture<*>>(size) })
    }

    fun getPasswordData(): ByteArray{
        return this.passwordFileInstance.data
    }

    private fun pushToCacheList(modelData: YsmModelData, version: YsmVersionMeta){
        val dataClassLoader: URLClassLoader = getCacheDataClassLoaderForVersion(version)!!
        val modelFileInstance: WrappedCacheData = WrappedCacheData.createFromModelData(
            modelData,
            dataClassLoader.loadClass(version.dataClassName),
            dataClassLoader as ClassLoader
        )

        val cacheData = modelFileInstance.toWritableBytes(modelFileInstance, this.passwordFileInstance.secretKey,
            this.passwordFileInstance.algorithmParameterSpec)
        val fileName = MD5Utils.getMd5(cacheData)

        //Ensure thread safe
        synchronized(this.modelToVersion2Caches){
            if (!this.modelToVersion2Caches.containsKey(modelFileInstance.getModelName())) {
                this.modelToVersion2Caches[modelFileInstance.getModelName()] = ConcurrentHashMap()
            }

            this.modelToVersion2Caches[modelFileInstance.getModelName()]!![version] = fileName

            synchronized(this.md5ToCacheData){
                if (!this.md5ToCacheData.containsKey(fileName)){
                    this.md5ToCacheData[fileName] = cacheData
                }
            }
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
        this.passwordFile = File(this.pluginInstance!!.dataFolder,"password.ysmdata")
        this.modJarFolder = File(this.pluginInstance!!.dataFolder,"mod_jars")
    }

    fun init(pluginInstance: Plugin){
        this.pluginInstance = pluginInstance

        this.initVars()
        this.loadPasswordFile()
        this.loadVersionMeta()
        this.downloadModJars()
    }
}