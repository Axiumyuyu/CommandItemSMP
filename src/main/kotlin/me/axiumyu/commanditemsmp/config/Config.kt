package me.axiumyu.commanditemsmp.config

import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP
import me.axiumyu.commanditemsmp.commands.CreateCmdItem.createFromConfig
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit.getServer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin.getPlugin

object Config {
    lateinit var config: FileConfiguration

    @JvmField
    var useMessage: Boolean = config.getBoolean("useMessage", true)

    @JvmField
    var drop: Boolean = config.getBoolean("drop", true)

    @JvmField
    val strict: Boolean = config.getBoolean("strict", true)

    private val cmdItems: MutableList<CmdItem> = mutableListOf()

    @JvmStatic
    fun getCmdItem(id: String): CmdItem? {
        var item: CmdItem? = null
        cmdItems.forEach {
            if (it.id == id) {
                item = it
            }
            null
        }
        return item
    }

    @JvmStatic
    fun addCmdItem(item: CmdItem) {
        cmdItems.add(item)
    }

    @JvmStatic
    fun removeCmdItem(item: CmdItem) {
        cmdItems.remove(item)
    }

    @JvmStatic
    fun getSize() = cmdItems.size

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
            when (e::class) {
                IllegalArgumentException::class -> {
                    getServer().sendMessage(text("物品材料存在问题,重新加载失败"))
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