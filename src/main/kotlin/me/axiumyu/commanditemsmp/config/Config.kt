package me.axiumyu.commanditemsmp.config

import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP
import me.axiumyu.commanditemsmp.commands.CreateCmdItem.createFromConfig
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit.getServer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin.getPlugin

object Config {
    lateinit var config : FileConfiguration

    var useMessage : Boolean = config.getBoolean("useMessage", true)

    @JvmField
    val cmdItems : MutableList<CmdItem> = mutableListOf()

    @JvmStatic
    fun reload() {
        getPlugin(CommandItemSMP::class.java).reloadConfig()
        val allCmdItem = config.getConfigurationSection("items") ?: return
        val backup = mutableListOf<CmdItem>()
        backup.addAll(cmdItems)
        try {
            cmdItems.clear()
            allCmdItem.getKeys(false).forEach {
                cmdItems.add(createFromConfig(config.getConfigurationSection("items.$it")!!))
            }
        } catch (e: Exception) {
            when (e::class){
                IllegalArgumentException::class -> {
                    getServer().sendMessage(text("config.yml 存在问题,重新加载失败"))
                }
                IndexOutOfBoundsException::class -> {
                    getServer().sendMessage(text("命名空间存在问题,重新加载失败"))
                }
                else -> {
                    getServer().sendMessage(text("未知错误,重新加载失败"))
                }
            }
            cmdItems.addAll(backup)
        }
    }

    @JvmStatic
    fun save() {
        getPlugin(CommandItemSMP::class.java).saveConfig()
    }
}