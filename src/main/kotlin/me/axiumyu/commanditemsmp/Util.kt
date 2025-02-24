package me.axiumyu.commanditemsmp

import io.papermc.paper.registry.RegistryAccess.registryAccess
import io.papermc.paper.registry.RegistryKey
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

object Util {

    @JvmStatic
    fun axiumyuKey(name : String) = NamespacedKey("axiumyu", name)

    @JvmField
    val TAG = axiumyuKey("tag")

    @JvmField
    val CMD = axiumyuKey("command")

    @JvmField
    val ID = axiumyuKey("id")

    @JvmField
    val NEED_PERM = axiumyuKey("need_perm")

    @JvmField
    val CD = axiumyuKey("cd")

    @JvmField
    val CONSUME = axiumyuKey("consume")

    @JvmField
    val castMap : HashMap<Char, String> =hashMapOf(
        'f' to "<white>",
        'e' to "<yellow>",
        'd' to "<light_purple>",
        'c' to "<red>",
        'b' to "<aqua>",
        'a' to "<green>",
        '9' to "<blue>",
        '8' to "<gray>",
        '7' to "<dark_gray>",
        '6' to "<gold>",
        '5' to "<purple>",
        '4' to "<dark_red>",
        '3' to "<dark_aqua>",
        '2' to "<dark_green>",
        '1' to "<dark_blue>",
        '0' to "<gray>"
    )

    @JvmStatic
    fun String.replaceColor(): String {
        var result = this
        for (index in result.indices) {
            if (result[index] == '&' && index + 1 < result.length) {
                val nextChar = result[index + 1]
                val replacement = castMap[nextChar] ?: nextChar.toString()
                result = result.substring(0, index) + replacement + result.substring(index + 2)
            }
        }
        return result
    }

    @JvmStatic
    fun String.replacePapi(pl : Player): String {
        return PlaceholderAPI.setPlaceholders(pl, this)
    }

    @JvmStatic
    fun nameSpace(name : String): NamespacedKey{
        val ns = name.split(":")
        return NamespacedKey(ns[0], ns[1])
    }

    @JvmStatic
    fun <T : Keyed> getRegistry(category: RegistryKey<T>, namespacedKey: NamespacedKey) = registryAccess().getRegistry(category).get(namespacedKey)
}