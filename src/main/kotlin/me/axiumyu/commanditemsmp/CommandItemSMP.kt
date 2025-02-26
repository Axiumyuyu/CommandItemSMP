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
        val mm by lazy { MiniMessage.miniMessage() }

        const val KEY = "commanditem"

        val config by lazy {getPlugin(CommandItemSMP::class.java).config }
    }

    override fun onEnable() {
        saveDefaultConfig()
        Config.reload()
        getCommand("getitem")?.setExecutor(GetCmdItem)
        getCommand("createitem")?.setExecutor(CreateCmdItem)
        server.pluginManager.registerEvents(UseCmdItem, this)
    }
}