package me.axiumyu.commanditemsmp.commands

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import io.papermc.paper.registry.RegistryKey.ATTRIBUTE
import io.papermc.paper.registry.RegistryKey.ENCHANTMENT
import me.axiumyu.commanditemsmp.CmdItem
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.KEY
import me.axiumyu.commanditemsmp.CommandItemSMP.Companion.mm
import me.axiumyu.commanditemsmp.Serialize.serialize
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
import me.axiumyu.commanditemsmp.config.Config
import me.axiumyu.commanditemsmp.config.Config.getSize
import org.bukkit.Bukkit.getServer
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
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import java.util.*


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
            (getSize() + 1).toString() + UUID.randomUUID()
        } else {
            p3[0]
        }
        if (Config.getCmdItem(key) != null) {
            p0.sendMessage("id已经存在")
            return false
        }
        try {
            serialize(item, key)
        } catch (e: IllegalArgumentException) {
            p0.sendMessage(e.message?:"序列化时发生错误")
        }
        p0.sendMessage("成功创建物品,id为 $key ! 请自行添加命令")
        Config.save()
        Config.reload()
        return true
    }

    @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
    fun createFromConfig(section: ConfigurationSection): CmdItem {
        val material = Material.valueOf(section.getString("material") ?: "STONE") //throw IllegalArgumentException
        val item = ItemStack(material)

        section.getConfigurationSection("attributes")?.let {
            val atts = getAttModFromConfig(section.getConfigurationSection("attributes")!!)
            atts.keys().forEach {
                atts.get(it).forEach { mod ->
                    item.addAttributeModifier(it, mod)
                }
            }
        }

        section.getConfigurationSection("enchantments")?.let {
            val enchs = parseEnchantments(it)
            item.addEnchantments(enchs)
        }
        item.editMeta {
            it.setRarity(ItemRarity.valueOf(section.getString("rarity") ?: "COMMON")) //throw IllegalArgumentException
            it.setMaxStackSize(section.getInt("max-stack", 1))
            it.setEnchantmentGlintOverride(section.getBoolean("glint", false))
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
        getServer().pluginManager.addPermission(Permission("commanditemsmp.use.$id", PermissionDefault.TRUE))
        return CmdItem(id, item, needPerm,cd,cmd, consume)
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