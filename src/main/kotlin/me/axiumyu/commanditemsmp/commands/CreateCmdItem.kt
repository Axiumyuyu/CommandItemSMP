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
        val attMod = section.getConfigurationSection("attribute-modifiers")
        val attList: MutableList<AttributeModifier> = mutableListOf()
        attMod?.getKeys(false)?.forEach {

            val att = attMod.getConfigurationSection(it)!!
            val id = att.getString("id") ?: return@forEach
            val amount = att.getDouble("amount", 0.0)
            val operation = AttributeModifier.Operation.valueOf(att.getString("operation") ?: return@forEach)
            val slot = EquipmentSlotGroup.getByName(att.getString("slot") ?: "HAND")
            val to = getRegistry(ATTRIBUTE, minecraft(att.getString("to") ?: "ATTACK_DAMAGE")) ?: return@forEach

            if (amount != 0.0 && slot != null) {
                attList.add(AttributeModifier(nameSpace(id), amount, operation, slot))
                item.addAttributeModifier(to, attList.last())
            }
        }
        val enchsec = section.getConfigurationSection("enchantments")
        val enchs = mutableMapOf<Enchantment, Int>()

        enchsec?.getKeys(false)?.forEach {
            val registry = getRegistry(ENCHANTMENT, nameSpace(it)) ?: return@forEach
            val level = enchsec.getInt(it, 1)
            enchs[registry] = level
        }
        item.addEnchantments(enchs)
        item.lore(section.getStringList("lore").map { mm.deserializeOrNull(it.replaceColor()) })
        item.editPersistentDataContainer a@{
            it.set(TAG, PersistentDataType.STRING, KEY)
            it.set(CMD, PersistentDataType.LIST.strings(), section.getStringList("command"))
            it.set(ID, PersistentDataType.STRING, section.name)
            it.set(CONSUME, PersistentDataType.BOOLEAN, section.getBoolean("consume", true))
            it.set(CD, PersistentDataType.INTEGER, section.getInt("cooldown", 0))
            it.set(NEED_PERM, PersistentDataType.BOOLEAN, section.getBoolean("need-permission", false))
        }
        return CmdItem(item)
    }


}