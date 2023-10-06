package me.earthme.mysm.events

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerChangeModelEvent(
    private val player: Player,
    private val targetModelLocation: NamespacedKey,
    private val targetModelTextureLocation: NamespacedKey
) : Event() , Cancellable {
    private var isCancelled: Boolean = false

    companion object{
        private val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return this.isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    fun getPlayer(): Player{
        return this.player
    }

    fun getTargetModelLocation(): NamespacedKey{
        return this.targetModelLocation
    }

    fun getTargetModelTextureLocation(): NamespacedKey{
        return this.targetModelTextureLocation
    }
}