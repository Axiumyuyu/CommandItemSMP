package me.axiumyu.commanditemsmp

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import io.papermc.paper.registry.RegistryKey.ATTRIBUTE
import io.papermc.paper.registry.RegistryKey.ENCHANTMENT
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.KEY
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.config
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.mm
import me.axiumyu.commanditemsmp.Util.CD
import me.axiumyu.commanditemsmp.Util.CMD
import me.axiumyu.commanditemsmp.Util.CONSUME
import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.NEED_PERM
import me.axiumyu.commanditemsmp.Util.TAG
import me.axiumyu.commanditemsmp.Util.getRegistry
import me.axiumyu.commanditemsmp.Util.nameSpace
import me.axiumyu.commanditemsmp.Util.propertyMap
import me.axiumyu.commanditemsmp.Util.replaceColor
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit.getServer
import org.bukkit.Material
import org.bukkit.NamespacedKey.minecraft
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.forEach

object Deserialize {
    @JvmStatic
    @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
    fun createFromConfig(section: ConfigurationSection): CmdItem {
        getServer().sendMessage(text("section: ${section.name}\nmaterial: ${config.getString("items.${section.name}.material")}"))
        val material = Material.valueOf(config.getString("items.${section.name}.material") ?: "STONE") //throw IllegalArgumentException
        val item = ItemStack(material)


        section.getConfigurationSection("attribute-modifiers")?.let {
            val atts = getAttModFromConfig(it)
            atts.keys().forEach {
                atts.get(it).forEach { mod ->
                    item.addAttributeModifier(it, mod)
                }
            }
        }

        section.getConfigurationSection("enchantments")?.let {
            val enchs = parseEnchantments(it)
            item.addUnsafeEnchantments(enchs)
        }
        item.editMeta {
            section.getString("name")?.let { n ->
                val name = mm.deserializeOrNull(n)?: return@let
                it.displayName(name)
            }

            section.getString("rarity")
                ?.let { r -> it.setRarity(ItemRarity.valueOf(r)) } //throw IllegalArgumentException
            section.getInt("max-stack").let { s ->
                if (s == 0) return@let
                it.setMaxStackSize(s)
            }
            section.getBoolean("glint").let { g ->
                it.setEnchantmentGlintOverride(g)
            }
            it.isUnbreakable = section.getBoolean("unbreakable", false)
        }

        item.lore(section.getStringList("lore").map { mm.deserializeOrNull(it.replaceColor()) })
        val cmd = section.getStringList(propertyMap[CMD]!!)
        val id = section.name
        val consume = section.getBoolean(propertyMap[CONSUME]!!, true)
        val cd = section.getInt(propertyMap[CD]!!, 0)
        val needPerm = section.getBoolean(propertyMap[NEED_PERM]!!, false)
        item.editPersistentDataContainer {
            it.set(TAG, PersistentDataType.STRING, KEY)
            it.set(CMD, PersistentDataType.LIST.strings(), cmd)
            it.set(ID, PersistentDataType.STRING, id)
            it.set(CONSUME, PersistentDataType.BOOLEAN, consume)
            it.set(CD, PersistentDataType.INTEGER, cd)
            it.set(NEED_PERM, PersistentDataType.BOOLEAN, needPerm)
        }
        val pm = getServer().pluginManager
        if (pm.getPermission("commanditemsmp.use.$id") == null) {
            pm.addPermission(Permission("commanditemsmp.use.$id", PermissionDefault.TRUE))
        }
        getServer().sendMessage(text("created item, id: $id, cmd: $cmd, consume: $consume, needPerm: $needPerm"))
        return CmdItem(id, item, needPerm, cd, cmd, consume)
    }

    private fun getAttModFromConfig(section: ConfigurationSection): Multimap<Attribute, AttributeModifier> {
        val attList: Multimap<Attribute, AttributeModifier> = ArrayListMultimap.create<Attribute, AttributeModifier>()
        section.getKeys(false).forEach {
            val to = getRegistry(ATTRIBUTE, minecraft(section.getString("$it.to") ?: "ATTACK_DAMAGE")) ?: return@forEach
            val att = parseAttributeModifier(section.getConfigurationSection(it)!!) ?: return@forEach
            attList.put(to, att)
        }
        return attList

    }

    private fun parseAttributeModifier(section: ConfigurationSection): AttributeModifier? {
        val id = section.name
        val amount = section.getDouble("amount", 0.0)
        val operation = AttributeModifier.Operation.valueOf(section.getString("operation") ?: return null) //throw IllegalArgumentException
        val slot = EquipmentSlotGroup.getByName(section.getString("slot") ?: "HAND") ?: return null
        return AttributeModifier(nameSpace(id), amount, operation, slot) //throw IndexOutOfBoundsException
    }

    private fun parseEnchantments(section: ConfigurationSection): Map<Enchantment, Int> {
        val enchs = mutableMapOf<Enchantment, Int>()
        section.getKeys(false).forEach {
            val registry = getRegistry(ENCHANTMENT, nameSpace(it)) ?: return@forEach
            val level = section.getInt(it, 1)
            enchs[registry] = level
        }
        return enchs
    }
}