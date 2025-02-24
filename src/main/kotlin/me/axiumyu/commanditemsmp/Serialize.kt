package me.axiumyu.commanditemsmp

import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.propertyMap
import me.axiumyu.commanditemsmp.config.Config.config
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING


object Serialize {

    @JvmStatic
    private fun String.set(key: String, value: Any) {
        if (value is Component) {
            config.setRichMessage("$this.$key", value)
        } else {
            config.set("$this.$key", value)
        }
    }

    @JvmStatic
    fun serialize(item: ItemStack, id: String? = null) {
        if (id == null && item.itemMeta?.persistentDataContainer?.has(ID, STRING) != true) {
            throw IllegalArgumentException("无法序列化物品，请指定物品ID。")
        }
        val id = id ?: item.itemMeta?.persistentDataContainer?.get(ID, STRING)
        val path = "items.$id"
        config.set("items.$id.material", item.type.name)

        val itemMeta = item.itemMeta
        if (itemMeta == null) return
        path.set("max-stack", itemMeta.maxStackSize)
        path.set("rarity", itemMeta.rarity.name)
        path.set("name", item.displayName())
        path.set("unbreakable", itemMeta.isUnbreakable)
        path.set("glint", itemMeta.enchantmentGlintOverride)

        itemMeta.lore()?.forEach {
            path.set("lore", it)
        }

        item.enchantments.forEach {
            path.set("enchantments.${it.key.key}", it.value)
        }
        val attMod: Map<Attribute?, Collection<AttributeModifier?>?>? = item.attributeModifiers?.asMap()
        attMod?.forEach { (att, value) ->
            value?.forEach {
                if (att?.key == null) return@forEach
                if (it?.key == null) return@forEach
                path.set("attribute-modifiers.${it.key}.to", att)
                path.set("attribute-modifiers.${it.key}.amount", it.amount)
                path.set("attribute-modifiers.${it.key}.operation", it.operation.name)
                path.set("attribute-modifiers.${it.key}.slot", it.slotGroup)
            }
        }
        val pdc = itemMeta.persistentDataContainer
        propertyMap.forEach { (key, value) ->
            if (key == ID) return@forEach
            if (pdc.has(key, STRING)) {
                path.set(value, pdc.get(key, STRING)!!)
            }
        }
    }
}