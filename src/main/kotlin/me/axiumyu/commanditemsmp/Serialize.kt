package me.axiumyu.commanditemsmp

import com.google.common.collect.Multimap
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.config
import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.propertyMap
import me.axiumyu.commanditemsmp.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit.getServer
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING


object Serialize {

    @JvmStatic
    private fun String.set(key: String, value: Any) {
        getServer().sendMessage(text("正在设置$key to $value"))
        if (value is Component) {
            config.setRichMessage("$this.$key", value)
        } else if (value !is Number) {
            config.set("$this.$key", if (value is String) uppercase() else value.toString().uppercase())
        }else{
            config.set("$this.$key", value)
        }
    }


    @JvmStatic
    fun serialize(item: ItemStack, id: String) {
        getServer().sendMessage(text("正在序列化物品, id: $id"))

        val path = "items.$id"
        path.set("material", item.type.name)

        val itemMeta = item.itemMeta ?: return
        if (itemMeta.hasRarity()) path.set("rarity", itemMeta.rarity.name)
        if (itemMeta.hasMaxStackSize()) path.set("max-stack", itemMeta.maxStackSize)
        if (itemMeta.hasDisplayName()) path.set("name", item.displayName())
        if (itemMeta.hasEnchantmentGlintOverride()) path.set("glint", itemMeta.enchantmentGlintOverride)
        path.set("unbreakable", itemMeta.isUnbreakable)

        itemMeta.lore()?.forEach {
            path.set("lore", it)
        }

        item.enchantments.forEach {
            path.set("enchantments.${it.key.key}", it.value)
        }
        val attMod: Multimap<Attribute, AttributeModifier>? = item.attributeModifiers
        attMod?.keys()?.forEach { att ->
            attMod.get(att).forEach {
                if (att?.key == null) return@forEach
                if (it?.key == null) return@forEach
                path.set("attribute-modifiers.${it.key}.amount", it.amount)
                path.set("attribute-modifiers.${it.key}.operation", it.operation.name)
                path.set("attribute-modifiers.${it.key}.slot", it.slotGroup)
                path.set("attribute-modifiers.${it.key}.to", att)
            }
        }
        val pdc = itemMeta.persistentDataContainer
        propertyMap.forEach { (key, value) ->
            if (key == ID) return@forEach
            if (pdc.has(key, STRING)) {
                path.set(value, pdc.get(key, STRING)!!)
            }
        }
        getServer().sendMessage(text("序列化完成"))
        Config.save()
    }
}