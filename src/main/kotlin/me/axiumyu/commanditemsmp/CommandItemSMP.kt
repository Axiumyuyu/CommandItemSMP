package me.axiumyu.commanditemsmp

import me.axiumyu.commanditemsmp.commands.CreateCmdItem
import me.axiumyu.commanditemsmp.commands.GetCmdItem
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.listener.UseCmdItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class CommandItemSMP : JavaPlugin() {
    companion object {
        lateinit var mm : MiniMessage

        const val KEY = "commanditem"
    }

    override fun onEnable() {
        Config.config = this.config
        saveDefaultConfig()
        mm = MiniMessage.miniMessage()
        getCommand("getitem")?.setExecutor(GetCmdItem)
        getCommand("createitem")?.setExecutor(CreateCmdItem)
        server.pluginManager.registerEvents(UseCmdItem, this)
    }
}