package me.earthme.mysm

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executor

object SchedulerUtils {
    fun schedulerAsExecutor(location: Location?): Executor{
        return Executor { task ->
            if (!MyYSM.isFolia){
                Bukkit.getScheduler().runTask(MyYSM.instance!!,task)
            }else{
                val bukkitClass = Bukkit::class.java
                val regionSchedulerMethod = bukkitClass.getDeclaredMethod("getRegionScheduler")
                val regionScheduler = regionSchedulerMethod.invoke(null)

                val regionSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")
                val executeMethod = regionSchedulerClass.getDeclaredMethod("execute",
                    Plugin::class.java,Location::class.java,Runnable::class.java)
                executeMethod.isAccessible = true
                executeMethod.invoke(regionScheduler,MyYSM.instance,location!!,task)
            }
        }
    }
}