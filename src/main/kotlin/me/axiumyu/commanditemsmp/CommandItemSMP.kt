package me.axiumyu.commanditemsmp

import me.axiumyu.commanditemsmp.commands.CreateCmdItem
import me.axiumyu.commanditemsmp.commands.GetCmdItem
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.listener.UseCmdItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class CommandItemSMP : JavaPlugin() {
    companion object {
        lateinit var mm : MiniMessage

        const val KEY = "commanditem"

        lateinit var config: FileConfiguration
    }

    override fun onEnable() {
        Companion.config = this.config
        saveDefaultConfig()
        mm = MiniMessage.miniMessage()
        Config.reload()
        getCommand("getitem")?.setExecutor(GetCmdItem)
        getCommand("createitem")?.setExecutor(CreateCmdItem)
        server.pluginManager.registerEvents(UseCmdItem, this)
    }
}