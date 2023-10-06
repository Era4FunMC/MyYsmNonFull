package me.earthme.mysm.network

import me.earthme.mysm.connection.FabricPlayerYsmConnection
import me.earthme.mysm.connection.ForgePlayerYsmConnection
import me.earthme.mysm.connection.PlayerYsmConnection
import me.earthme.mysm.utils.AsyncExecutor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport


object MainYsmNetworkHandler : Listener{
    private val tickExecutor: Executor = CompletableFuture.delayedExecutor(50,TimeUnit.MILLISECONDS, AsyncExecutor.ASYNC_EXECUTOR_INSTANCE)

    private val shouldTickNext: AtomicBoolean = AtomicBoolean(true)
    private val hasScheduledTask: AtomicBoolean = AtomicBoolean(false)
    private val isTicking: AtomicBoolean = AtomicBoolean(false)

    val modInstalledPlayerList: MutableList<Player> = CopyOnWriteArrayList()
    private val visibleMap: MutableMap<Player,MutableSet<Player>> = ConcurrentHashMap()
    private val connectionMap: MutableMap<Player,PlayerYsmConnection> = ConcurrentHashMap()
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

        tickExecutor.execute {
            try {
                hasScheduledTask.set(false)
                this.doTick()
            }finally {
                //Schedule the next tick
                tickExecutor.execute{
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
                    connectionMap[singlePlayer]?.tick()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }finally{
            isTicking.set(false)
        }
    }

    fun Player.getConnection(): PlayerYsmConnection{
        return connectionMap[this]!!
    }

    @EventHandler
    fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent){
        this.connectionMap[playerJoinEvent.player] = if (playerJoinEvent.player.clientBrandName == "forge"){
            ForgePlayerYsmConnection(playerJoinEvent.player,this.pluginInstance!!)
        }else{
            FabricPlayerYsmConnection(playerJoinEvent.player,this.pluginInstance!!)
        }

        this.connectionMap[playerJoinEvent.player]!!.onPlayerJoin(playerJoinEvent.player)
        //Refresh tracker once
        this.updateTracker(playerJoinEvent.player)
    }

    //TODO Optimize
    @EventHandler
    fun onPlayerMove(playerMoveEvent: PlayerMoveEvent) {
        val player = playerMoveEvent.player
        this.updateTracker(player)
    }

    private fun updateTracker(player: Player){
        for (singlePlayer: Player in Bukkit.getOnlinePlayers()) {
            if (!this.visibleMap.containsKey(player)) {
                this.visibleMap[player] = HashSet()
                this.visibleMap[player]!!.add(player) //The player can always see itself
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
        player.getConnection().onTrackerUpdate(hasSeen)
    }
    
    @EventHandler
    fun onPlayerLeftGame(playerQuitEvent: PlayerQuitEvent){
        playerQuitEvent.player.getConnection().onPlayerLeft(playerQuitEvent.player)
        this.connectionMap.remove(playerQuitEvent.player)
    }

    fun sendReloadToAllPlayers(){
        //Broadcast to all
        for (player in Bukkit.getOnlinePlayers()){
            val connection = player.getConnection()
            if (connection is FabricPlayerYsmConnection){
                connection.sendReload()
            }else if (connection is ForgePlayerYsmConnection){
                connection.sendReload()
            }
        }
    }

    fun getModInstalledPlayers(): List<Player>{
        return this.modInstalledPlayerList //Return a immutable list
    }

    //Inbound packet receiver
    class PluginMessageListenerYSM : PluginMessageListener {
        override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray?) {
            player.getConnection().onMessageIncoming(NamespacedKey.fromString(channel)!!,message!!)
        }
    }
}