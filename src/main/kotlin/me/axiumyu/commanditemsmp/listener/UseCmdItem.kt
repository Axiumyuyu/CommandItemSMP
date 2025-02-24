package me.axiumyu.commanditemsmp.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.net.http.WebSocket

object UseCmdItem : Listener {

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        if (event.item ==null) return
        if (!event.action.isRightClick) return

    }
}