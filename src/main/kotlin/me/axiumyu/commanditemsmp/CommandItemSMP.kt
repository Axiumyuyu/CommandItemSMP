package me.axiumyu.commanditemsmp

import me.axiumyu.commanditemsmp.Util.axiumyuKey
import me.axiumyu.commanditemsmp.config.Config
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

class CommandItemSMP : JavaPlugin() {
    companion object {
        lateinit var mm : MiniMessage

        const val KEY = "commanditem"
    }

    override fun onEnable() {
        Config.config = this.config
        mm = MiniMessage.miniMessage()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
