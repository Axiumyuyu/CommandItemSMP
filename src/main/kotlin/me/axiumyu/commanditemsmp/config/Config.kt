package me.axiumyu.commanditemsmp.config

import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP
import me.axiumyu.commanditemsmp.commands.CreateCmdItem.createFromConfig
import org.apache.commons.lang3.mutable.Mutable
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
        } catch (e: IllegalArgumentException) {
            cmdItems.addAll(backup)
            getServer().sendMessage("config.yml 存在问题,重新加载失败")
        }
    }

    @JvmStatic
    fun save() {
        getPlugin(CommandItemSMP::class.java).saveConfig()
    }
}