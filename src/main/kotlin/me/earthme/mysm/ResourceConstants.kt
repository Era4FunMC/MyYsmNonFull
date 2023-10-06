package me.earthme.mysm

import me.earthme.mysm.utils.FileUtils
import org.bukkit.plugin.Plugin
import java.io.InputStream

object ResourceConstants {
    var defaultMainAnimationJsonContent: String? = null
    var defaultArmAnimationJsonContent: String? = null
    var defaultExtraAnimationJsonContent: String? = null

    private var loadedAll = false

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