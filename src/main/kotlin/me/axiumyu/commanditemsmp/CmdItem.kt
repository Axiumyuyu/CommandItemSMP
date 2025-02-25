package me.axiumyu.commanditemsmp

import me.axiumyu.commanditemsmp.Util.replacePapi
import me.axiumyu.commanditemsmp.config.Config
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import java.util.UUID

data class CmdItem(
    val id : String,
    val item: ItemStack,
    var needPerm : Boolean,
    var cooldown : Int,
    var command : List<String>,
    var consume : Boolean
) {


    companion object {
        @JvmStatic
        fun Player.sendInfo(msg: String) {
            if (Config.useMessage) {
                this.sendMessage(msg)
            } else {
                this.sendActionBar(Component.text().content(msg).build())
            }
        }
    }

    val lastUse = mutableMapOf<UUID, Long>()

    fun canUse(pl: Player): Boolean {
        if (needPerm && !pl.hasPermission("commanditemsmp.use.$id")) {
            pl.sendInfo("你没有权限使用这个物品。")
            return false
        }
        if (cooldown > 0) {
            if (lastUse.contains(pl.uniqueId) && System.currentTimeMillis() - lastUse[pl.uniqueId]!! < cooldown * 1000) {
                pl.sendInfo("你需要等待${(cooldown - (System.currentTimeMillis() - lastUse[pl.uniqueId]!!) / 1000).toInt()}秒才能再次使用这个物品。")
                return false
            }
        }
        return true
    }

    fun useItem(pl: Player) {
        if (!canUse(pl)) return
        lastUse[pl.uniqueId] = System.currentTimeMillis()
        val server = getPlugin(CommandItemSMP::class.java).server
        command.forEach {
            server.dispatchCommand(server.consoleSender, it.replacePapi(pl))
        }
        if (consume) {
            pl.inventory.removeItem(item)
        }
    }
}