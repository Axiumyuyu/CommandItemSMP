package me.axiumyu.commanditemsmp.commands

import io.papermc.paper.registry.RegistryKey.ATTRIBUTE
import io.papermc.paper.registry.RegistryKey.ENCHANTMENT
import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.KEY
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.mm
import me.axiumyu.commanditemsmp.Util.CD
import me.axiumyu.commanditemsmp.Util.CMD
import me.axiumyu.commanditemsmp.Util.CONSUME
import me.axiumyu.commanditemsmp.Util.ID
import me.axiumyu.commanditemsmp.Util.NEED_PERM
import me.axiumyu.commanditemsmp.Util.TAG
import me.axiumyu.commanditemsmp.Util.getRegistry
import me.axiumyu.commanditemsmp.Util.nameSpace
import me.axiumyu.commanditemsmp.Util.replaceColor
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.config.Config.cmdItems
import me.axiumyu.commanditemsmp.config.Config.config
import org.bukkit.Material
import org.bukkit.NamespacedKey.minecraft
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.lang.IndexOutOfBoundsException


object CreateCmdItem : CommandExecutor {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        if (!p0.hasPermission("commanditemsmp.create")) {
            p0.sendMessage("你没有权限使用这个命令")
            return false
        }
        if (p0 !is Player) {
            p0.sendMessage("这个命令只能玩家使用")
            return false
        }
        if (p0.inventory.itemInMainHand.isEmpty) {
            p0.sendMessage("你的主手没有物品")
            return false
        }
        val item = p0.inventory.itemInMainHand
        val key = if (p3.isEmpty()) {
            (cmdItems.size + 1).toString()
        } else {
            p3[0]
        }
        config["items.${key}"] = item
        p0.sendMessage("成功创建物品,id为 $key ! 请自行添加命令")
        Config.save()
        Config.reload()
        return true
    }

    @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
    fun createFromConfig(section: ConfigurationSection): CmdItem {
        val material = Material.valueOf(section.getString("material") ?: "STONE")
        val item = ItemStack(material)

        section.getConfigurationSection("attributes")?.let {
            val atts = getAttModFromConfig(section.getConfigurationSection("attributes")!!)
            atts.forEach { att ->
                att.value.forEach {
                    item.addAttributeModifier(att.key, it)
                }
            }
        }

        section.getConfigurationSection("enchantments")?.let{
            val enchs = parseEnchantments(it)
            item.addEnchantments(enchs)
        }

        item.lore(section.getStringList("lore").map { mm.deserializeOrNull(it.replaceColor()) })
        item.editPersistentDataContainer {
            it.set(TAG, PersistentDataType.STRING, KEY)
            it.set(CMD, PersistentDataType.LIST.strings(), section.getStringList("command"))
            it.set(ID, PersistentDataType.STRING, section.name)
            it.set(CONSUME, PersistentDataType.BOOLEAN, section.getBoolean("consume", true))
            it.set(CD, PersistentDataType.INTEGER, section.getInt("cooldown", 0))
            it.set(NEED_PERM, PersistentDataType.BOOLEAN, section.getBoolean("need-permission", false))
        }
        return CmdItem(item)
    }

    private fun getAttModFromConfig(section: ConfigurationSection): Map<Attribute, List<AttributeModifier>> {
        val attList: MutableMap<Attribute, MutableList<AttributeModifier>> = mutableMapOf()
        section.getKeys(false).forEach {
            val to = getRegistry(ATTRIBUTE, minecraft(section.getString("$it.to") ?: "ATTACK_DAMAGE")) ?: return@forEach
            val att = parseAttributeModifier(section.getConfigurationSection(it)!!) ?: return@forEach
            if (attList.contains(to)) {
                attList[to]!!.add(att)
            }else{
                attList[to] = mutableListOf(att)
            }
        }
        return attList

    }

    private fun parseAttributeModifier(section: ConfigurationSection): AttributeModifier? {
        val id = section.name
        val amount = section.getDouble("amount", 0.0)
        val operation = AttributeModifier.Operation.valueOf(section.getString("operation") ?: return null)
        val slot = EquipmentSlotGroup.getByName(section.getString("slot") ?: "HAND") ?: return null
        return AttributeModifier(nameSpace(id), amount, operation, slot)
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