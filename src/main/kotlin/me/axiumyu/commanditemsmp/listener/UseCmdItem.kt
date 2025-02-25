package me.axiumyu.commanditemsmp.listener

import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.KEY
import me.axiumyu.commanditemsmp.Util.CD
import me.axiumyu.commanditemsmp.Util.CMD
import me.axiumyu.commanditemsmp.Util.CONSUME
import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.NEED_PERM
import me.axiumyu.commanditemsmp.Util.TAG
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.config.Config.getCmdItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.STRING

object UseCmdItem : Listener {

    @EventHandler
    fun onUse(event: PlayerInteractEvent) {
        val item = event.item
        if (item == null) return
        if (!event.action.isRightClick) return
        val pdc = item.persistentDataContainer
        if (!pdc.has(TAG, STRING) || pdc.get(TAG, STRING) != KEY) return
        if (!pdc.has(ID, STRING)) return
        val cmdItem = getCmdItem(pdc.get(ID, STRING)!!) ?: return
        if (!Config.strict){
            cmdItem.needPerm = pdc.get(NEED_PERM, PersistentDataType.BOOLEAN) == true
            cmdItem.command = pdc.get(CMD, PersistentDataType.LIST.strings())?: listOf<String>()
            cmdItem.cooldown = pdc.get(CD, PersistentDataType.INTEGER)?: 0
            cmdItem.consume = pdc.get(CONSUME, PersistentDataType.BOOLEAN) == true
        }
       val pl = event.player
       if (cmdItem.canUse(pl)) cmdItem.useItem(pl)
    }
}