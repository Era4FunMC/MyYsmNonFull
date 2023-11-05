package me.earthme.mysm.network

import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage
import io.netty.buffer.Unpooled
import me.earthme.mysm.utils.SchedulerUtils
import me.earthme.mysm.network.connection.FabricPlayerYsmConnection
import me.earthme.mysm.network.connection.ForgePlayerYsmConnection
import me.earthme.mysm.network.connection.PlayerYsmConnection
import me.earthme.mysm.utils.AsyncExecutor
import me.earthme.mysm.utils.mc.MCPacketCodecUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport


object YsmClientConnectionManager : Listener, SimplePacketListenerAbstract(PacketListenerPriority.HIGH) {
    private val tickScheduler: Executor = CompletableFuture.delayedExecutor(50,TimeUnit.MILLISECONDS, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE)

    private val shouldTickNext: AtomicBoolean = AtomicBoolean(true)
    private val hasScheduledTask: AtomicBoolean = AtomicBoolean(false)
    private val isTicking: AtomicBoolean = AtomicBoolean(false)

    val modInstalledPlayerList: MutableList<Player> = CopyOnWriteArrayList()
    private val visibleMap: MutableMap<Player,MutableSet<Player>> = ConcurrentHashMap()
    private val connectionMap: MutableMap<Player, PlayerYsmConnection> = ConcurrentHashMap()
    private var pluginInstance: Plugin? = null

    fun init(plugin: Plugin){
        this.pluginInstance = plugin
    }
    
    fun awaitShutdown(){
        shouldTickNext.set(false) //Shutdown tick loop
        while (isTicking.get() || hasScheduledTask.get()){ //Wait it for terminating
            LockSupport.parkNanos("AwaitShuttingDown",1_000_000)
        }
    }

    fun tickThenSchedule(){
        //If we don't want tick it anymore,Cancel the next tick
        if (!shouldTickNext.get()){
            return
        }

        tickScheduler.execute {
            try {
                hasScheduledTask.set(false)
                this.doTick()
            }finally {
                //Schedule the next tick
                tickScheduler.execute{
                    hasScheduledTask.set(true)
                    this.tickThenSchedule()
                }
            }
        }
    }

    private fun doTick(){
        isTicking.set(true)
        try {
            //Skip if there is no player
            if (Bukkit.getOnlinePlayers().isEmpty()){
                return
            }

            try {
                for (singlePlayer in  Bukkit.getOnlinePlayers()){
                    SchedulerUtils.schedulerAsExecutor(singlePlayer.location).execute {
                        connectionMap[singlePlayer]?.tick()
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }finally{
            isTicking.set(false)
        }
    }

    fun Player.getConnection(): PlayerYsmConnection?{
        return connectionMap[player]
    }

    @EventHandler
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val player = playerMoveEvent.player
        this.updateTracker(player)
    }

    private fun updateTracker(player: Player){
        for (singlePlayer: Player in Bukkit.getOnlinePlayers()) {
            if (!this.visibleMap.containsKey(player)) {
                this.visibleMap[player] = HashSet()
                this.visibleMap[player]!!.add(player)
            }

            if (player.canSee(singlePlayer) && !this.visibleMap[player]!!.contains(singlePlayer)) {
                this.visibleMap[player]!!.add(singlePlayer)
                this.playerTrackedPlayer(player,singlePlayer)
            }else if (!player.canSee(singlePlayer)){
                this.visibleMap[player]!!.remove(singlePlayer)
            }
        }
    }

    private fun playerTrackedPlayer(player: Player, hasSeen: Player){
        player.getConnection()?.onTrackerUpdate(hasSeen)
    }
    
    @EventHandler
    fun onPlayerLeftGame(playerQuitEvent: PlayerQuitEvent){
        if (!this.connectionMap.containsKey(playerQuitEvent.player)){
            return
        }

        playerQuitEvent.player.getConnection()?.onPlayerLeft(playerQuitEvent.player)
        this.connectionMap.remove(playerQuitEvent.player)
        this.visibleMap.remove(playerQuitEvent.player)
    }

    fun sendReloadToAllPlayers(){
        for (player in Bukkit.getOnlinePlayers()){
            player.getConnection()?.sendReload()
        }
    }

    fun getModInstalledPlayers(): List<Player>{
        return this.modInstalledPlayerList //Return a immutable list
    }

    override fun onPacketPlayReceive(event: PacketPlayReceiveEvent){
        if (event.packetType == PacketType.Play.Client.PLUGIN_MESSAGE){
            val player: Player = event.player as Player
            val wrappedPluginMessage = WrapperPlayClientPluginMessage(event)

            val channelName = wrappedPluginMessage.channelName
            val channelData = wrappedPluginMessage.data

            this.connectionMap[player]?.let{
                if (it.isChannelNameMatched(channelName)){
                    it.onMessageIncoming(NamespacedKey.fromString(channelName)!!,channelData)
                }
            }

            if (channelName == "minecraft:brand"){
                val wrappedData = Unpooled.copiedBuffer(channelData)
                val brand = MCPacketCodecUtils.readUtf(32767,wrappedData)

                if (this.connectionMap.containsKey(player)){
                    return
                }

                this.connectionMap[player] = if (brand.contains("fabric")){
                    FabricPlayerYsmConnection(player,this.pluginInstance!!)
                }else{
                    ForgePlayerYsmConnection(player,this.pluginInstance!!)
                }

                player.getConnection()?.onPlayerJoin(player)
                this.updateTracker(player)
            }
        }
    }
}