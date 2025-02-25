package me.axiumyu.commanditemsmp.config

import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.config
import me.axiumyu.commanditemsmp.commands.CreateCmdItem.createFromConfig
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit.getServer
import org.bukkit.plugin.java.JavaPlugin.getPlugin

object Config {


    @JvmField
    var useMessage: Boolean = config.getBoolean("useMessage", true)

    @JvmField
    var drop: Boolean = config.getBoolean("drop", true)

    @JvmField
    var strict: Boolean = config.getBoolean("strict", true)

    private val cmdItems: MutableList<CmdItem> = mutableListOf()

    @JvmStatic
    fun getCmdItem(id: String): CmdItem? {
        var item: CmdItem? = null
        cmdItems.forEach {
            if (it.id == id) {
                item = it
            }
        }
        return item
    }

    @JvmStatic
    fun getSize() = cmdItems.size

    @JvmStatic
    fun reload() {
        getPlugin(CommandItemSMP::class.java).reloadConfig()
        useMessage = config.getBoolean("useMessage", true)
        drop = config.getBoolean("drop", true)
        strict = config.getBoolean("strict", true)
        val backup = mutableListOf<CmdItem>()
        backup.addAll(cmdItems)
        backup.forEach {
            getServer().sendMessage(text(it.toString()))
        }
        try {
            cmdItems.clear()
            config.getConfigurationSection("items")?.getKeys(false)?.forEach {
                cmdItems.add(createFromConfig(config.getConfigurationSection("items.$it")!!))
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        }finally {
            val pm = getServer().pluginManager
            pm.permissions.filter {
                it.name.startsWith("commanditemsmp.use.") &&
                getCmdItem(it.name.replace("commanditemsmp.use.", "")) == null
            }.forEach {
                pm.removePermission(it)
            }
        }
    }

    @JvmStatic
    fun save() {
        getPlugin(CommandItemSMP::class.java).saveConfig()
    }
}