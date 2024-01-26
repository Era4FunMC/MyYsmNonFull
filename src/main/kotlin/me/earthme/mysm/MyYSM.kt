package me.earthme.mysm

import me.earthme.mysm.bstats.Metrics
import me.earthme.mysm.bstats.Metrics.SimplePie
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.Callable

class MyYSM : JavaPlugin() {

    override fun onEnable() {
        instance = this
        this.saveDefaultConfig()

        //Check it if is folia
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            isFolia = true
        }catch (_: Exception){ }

        metrics = Metrics(this,19774)
        metrics.addCustomChart(SimplePie("server_brand", Callable {
            return@Callable Bukkit.getServer().name
        }))
        metrics.addCustomChart(SimplePie("is_folia", Callable {
            return@Callable isFolia.toString()
        }))

        //Loading message
        this.logger.info("--------------------------------------------------")
        this.logger.info("      __  ___    __  __             ")
        this.logger.info("     /  |/  /_  _\\ \\/ /________ ___ ")
        this.logger.info("    / /|_/ / / / /\\  / ___/ __ `__ \\")
        this.logger.info("   / /  / / /_/ / / (__  ) / / / / /")
        this.logger.info("  /_/  /_/\\__, / /_/____/_/ /_/ /_/ ")
        this.logger.info("         /____/                     ")
        this.logger.info("                                     ")
        this.logger.info(" Version: 1.7.Universal Author: MoliaMC(Era4FunMC)")
        this.logger.info("--------------------------------------------------")

        this.loadConfigValues()
        PluginBootstrap.initAll(this)
    }

    private fun loadConfigValues(){
        defaultModelLocation = NamespacedKey.fromString(config.getString("default_model_loc")!!)
        defaultModelTextureLocation = NamespacedKey.fromString(config.getString("default_model_texture_loc")!!)
        languageName = config.getString("language")
    }

    override fun onDisable() {
        PluginBootstrap.unloadAll(this)
        instance = null
    }

    companion object{
        var instance: MyYSM? = null
            private set
        lateinit var metrics: Metrics private set
        var isFolia: Boolean = false

        var defaultModelLocation: NamespacedKey? = null
        var defaultModelTextureLocation: NamespacedKey? = null
        var languageName: String? = "en_US"
    }
}