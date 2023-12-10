package me.earthme.mysm

import me.earthme.mysm.utils.FileUtils
import org.bukkit.plugin.Plugin
import java.io.InputStream

object ResourceConstants {
    @JvmStatic var defaultMainAnimationJsonContent: String? = null
    @JvmStatic var defaultArmAnimationJsonContent: String? = null
    @JvmStatic var defaultExtraAnimationJsonContent: String? = null

    @JvmStatic private var loadedAll = false


    /**
     * 调用这个方法用来初始化jar包内自带的一些ysm的默认的模型数据，一般用来加载模型的时候补全的(),
     * 这个方法只能调用一次,如果第二次调用，它会忽略掉，因为已经加载过一次了
     * @param pluginInstance 插件的实例
     */
    @JvmStatic
    fun initAll(pluginInstance: Plugin){
        if (loadedAll){
            pluginInstance.logger.info("All constant has loaded!Skipping")
        }

        try {
            try {
                pluginInstance.logger.info("Reading jar resources")
                val mainAnimationFileStream: InputStream? = ResourceConstants::class.java.classLoader.getResourceAsStream("ysm_data/main.animation.json")
                val armAnimationFileStream: InputStream? = ResourceConstants::class.java.classLoader.getResourceAsStream("ysm_data/arm.animation.json")
                val extraAnimationFileStream: InputStream? = ResourceConstants::class.java.classLoader.getResourceAsStream("ysm_data/extra.animation.json")

                defaultMainAnimationJsonContent = String(FileUtils.readInputStreamToByte(mainAnimationFileStream!!)!!)
                defaultArmAnimationJsonContent = String(FileUtils.readInputStreamToByte(armAnimationFileStream!!)!!)
                defaultExtraAnimationJsonContent = String(FileUtils.readInputStreamToByte(extraAnimationFileStream!!)!!)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }finally {
            loadedAll = true
        }
    }
}