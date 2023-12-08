package me.earthme.mysm.utils

import me.earthme.mysm.MyYSM
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import java.util.concurrent.Executor

/**
 * 这个类用于与BukkitScheduler或者FoliaRegionScheduler交互，也是实现folia和bukkit双向兼容的重要部分()
 */
object SchedulerUtils {
    /**
     * 获取一个executor包装的主线程scheduler
     * @param location 玩家或者这个区域的位置，用于folia兼容的实现
     * @return executor实例
     */
    fun BukkitScheduler.schedulerAsExecutor(location: Location?): Executor{
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
                executeMethod.invoke(regionScheduler, MyYSM.instance,location!!,task)
            }
        }
    }
}