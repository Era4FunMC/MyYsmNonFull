package me.earthme.mysm

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.InputStreamReader

object I18nManager {
    @JvmStatic private val languageFile : YamlConfiguration = YamlConfiguration()

    fun initLanguageFile(pluginInstance: Plugin,languageName: String){
        pluginInstance.logger.info("Loading language file.")

        var internalInputStream = I18nManager::class.java.classLoader.getResourceAsStream("lang/${languageName}.yml")
        if (internalInputStream == null){
            pluginInstance.logger.warning("The language file set by config has not found!Falling back to en_US")
            internalInputStream = I18nManager::class.java.classLoader.getResourceAsStream("lang/en_US.yml")
        }

        val configReader = InputStreamReader(internalInputStream!!,"UTF-8")
        configReader.use { reader ->
            languageFile.load(reader)
        }
        pluginInstance.logger.info("Loaded language file!")
    }

    fun parseTranslatableKey(messageKey: String, vararg args: Any?): String {
        val targetMessageUnmapped = this.languageFile.getString(messageKey)
            ?: throw IllegalArgumentException("Target translatable key has not found!")

        return String.format(targetMessageUnmapped, *args)
    }

    fun parseTranslatableKey(messageKey: String): String {
        return languageFile.getString(messageKey)
            ?: throw IllegalArgumentException("Target translatable key has not found!")
    }
}