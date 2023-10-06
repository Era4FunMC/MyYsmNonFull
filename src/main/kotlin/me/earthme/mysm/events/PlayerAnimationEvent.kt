package me.earthme.mysm.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerAnimationEvent(
    private val player: Player,
    private val animation: String
): Event() , Cancellable {
    companion object{
        private val handlerList = HandlerList()
    }
    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    fun getPlayer(): Player{
        return this.player
    }

    fun getAnimation(): String{
        return this.animation
    }
}