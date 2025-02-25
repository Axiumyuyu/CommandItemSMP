package me.axiumyu.commanditemsmp.commands

import me.axiumyu.commanditemsmp.config.Config.drop
import me.axiumyu.commanditemsmp.config.Config.getCmdItem
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GetCmdItem : CommandExecutor {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        if (!p0.hasPermission("commanditemsmp.getitem")) return false
        if (p0 !is Player) return false
        if (p3.isEmpty()) return false
        val id = p3[0]
        val i = p0.inventory.firstEmpty()
        if (i == -1 && !drop) {
            p0.sendMessage("§c背包已满")
            return false
        }
        val item = getCmdItem(id)
        if (item == null) {
            p0.sendMessage("§c未找到物品")
            return false
        }
        if (i != -1) {
            p0.inventory.setItem(i, item.item)
        } else {
            p0.world.dropItem(p0.location, item.item){
                it.isGlowing = true
            }
        }
        return true
    }
}