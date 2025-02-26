package me.axiumyu.commanditemsmp.commands

import me.axiumyu.commanditemsmp.Serialize.serializeToConfig
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.config.Config.getSize
import net.kyori.adventure.text.Component.text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*


object CreateCmdItem : CommandExecutor {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        if (p3.isNotEmpty() && p3[0] == "reload") {
            if (p0.hasPermission("commanditemsmp.reload")) {
                Config.reload()
                p0.sendMessage {
                    text {
                        "重载配置文件"
                    }
                }
                return true
            }
        }
        if (p3.isNotEmpty() && p3[0] == "save") {
            if (p0.hasPermission("commanditemsmp.save")) {
                Config.save()
                p0.sendMessage {
                    text("保存配置文件")
                }
                return true
            }
        }
        if (!p0.hasPermission("commanditemsmp.create")) {
            p0.sendMessage("你没有权限使用这个命令")
            return false
        }
        if (p0 !is Player) {
            p0.sendMessage("这个命令只能玩家使用")
            return false
        }
        val item = p0.inventory.itemInMainHand
        if (item.isEmpty) {
            p0.sendMessage("你的主手没有物品")
            return false
        }
        val key = if (p3.isEmpty()) {
            (getSize() + 1).toString() + UUID.randomUUID()
        } else {
            p3[0]
        }
        if (Config.getCmdItem(key) != null) {
            p0.sendMessage("id已经存在")
            return false
        }
        try {
            serializeToConfig(item, key, true)
        } catch (e: IllegalArgumentException) {
            p0.sendMessage(e.message ?: "序列化时发生错误")
        }
        p0.sendMessage("成功创建物品,id为 $key ! 请自行添加命令")
        return true
    }
}