package me.axiumyu.commanditemsmp

import com.google.common.collect.Multimap
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.config
import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.propertyMap
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
    private val Any.out : String
        get() = this.toString().uppercase()


    @JvmStatic
    fun serialize(item: ItemStack, id: String? = null) {
        if (id == null && item.itemMeta?.persistentDataContainer?.has(ID, STRING) != true) {
            throw IllegalArgumentException("无法序列化物品，请指定物品ID。")
        }
        val id = id ?: item.itemMeta?.persistentDataContainer?.get(ID, STRING)
        val path = "items.$id"
        path.set("material", item.type.name.out)

        val itemMeta = item.itemMeta ?: return
        if (itemMeta.hasRarity()) path.set("rarity", itemMeta.rarity.name.out)
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
                path.set("attribute-modifiers.${it.key}.operation", it.operation.name.out)
                path.set("attribute-modifiers.${it.key}.slot", it.slotGroup.out)
                path.set("attribute-modifiers.${it.key}.to", att.out)
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