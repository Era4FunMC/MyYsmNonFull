package me.earthme.mysm.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerExtraAnimationEvent(
    private val player: Player,
    private val extraId: Int
) : Event(),Cancellable{
    private var cancelled = false

    companion object{
        private val handlerList: HandlerList = HandlerList()
    }

    fun getPlayer(): Player{
        return this.player
    }

    fun getExtraId(): Int{
        return this.extraId
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}