package com.skillw.randomitem.internal.meta

import com.google.common.base.Enums
import com.skillw.pouvoir.api.script.ScriptTool
import com.skillw.pouvoir.util.ColorUtils.decolored
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.meta.BuilderMeta
import com.skillw.randomitem.api.meta.MetaMeta
import com.skillw.randomitem.api.meta.TagMeta
import com.skillw.randomitem.util.ListUtils.nextLineToNew
import org.bukkit.Color
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.LeatherArmorMeta
import taboolib.common.platform.function.warning
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.library.xseries.XEnchantment
import taboolib.library.xseries.XMaterial.supports
import taboolib.library.xseries.parseToMaterial
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.ItemTagList
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.getName
import taboolib.platform.util.hasLore
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern


val displayMeta by lazy {
    BuilderMeta("display",
        0,
        "NULL",
        "NULL",
        Function {
            return@Function it.getName().decolored()
        }
    ) {
        val data = it.first
        val builder = it.second
        val value = data["display-context"] as? String? ?: return@BuilderMeta builder.name
        builder.name = data.handle(value)
        return@BuilderMeta builder.name
    }
}

val materialMeta by lazy {
    BuilderMeta("material", 1, "NULL",
        "NULL",
        Function {
            return@Function it.type.name
        }) {
        val data = it.first
        val builder = it.second
        val value = data["material-context"] as? String? ?: return@BuilderMeta builder.material
        builder.material = data.handle(value).parseToMaterial()
        return@BuilderMeta builder.material
    }
}

@Suppress("DEPRECATION")
val damageMeta by lazy {
    BuilderMeta("damage", 2, "NULL",
        "NULL",
        Function {
            if (majorLegacy >= 11300) {
                if (!it.hasItemMeta()) return@Function null
                val itemMeta = it.itemMeta
                if (itemMeta !is Damageable || !itemMeta.hasDamage()) return@Function null
                return@Function itemMeta.damage
            }
            return@Function it.durability
        }) {
        val data = it.first
        val builder = it.second
        val value = Coerce.toInteger(data.handle(data["data-context"]?.toString() ?: return@BuilderMeta null))
        builder.damage = value
        return@BuilderMeta value
    }
}

@Suppress("DEPRECATION")
val dataMeta by lazy {
    BuilderMeta("data", 3, "NULL",
        "NULL",
        Function {
            if (!it.hasItemMeta()) return@Function null
            if (majorLegacy >= 11400) {
                val itemMeta = it.itemMeta!!
                if (itemMeta.hasCustomModelData()) {
                    return@Function itemMeta.customModelData
                }
            }
            return@Function null
        }) {
        val data = it.first
        val builder = it.second
        val value = Coerce.toInteger(data.handle(data["data-context"]?.toString() ?: return@BuilderMeta null))
        if (majorLegacy >= 11400) {
            builder.customModelData = value
        }
        return@BuilderMeta value
    }
}


val loreMeta by lazy {
    BuilderMeta("lore", 4, "NULL",
        "NULL",
        Function {
            return@Function if (it.hasLore()) it.itemMeta!!.lore!!.decolored() else null
        }) {
        val data = it.first
        val builder = it.second
        val value = Coerce.toListOf(data["lore-context"], String::class.java) ?: return@BuilderMeta builder.lore
        val list = data.handle(value).nextLineToNew()
        builder.lore.addAll(list)
        return@BuilderMeta builder.lore
    }
}

val unbreakableMeta by lazy {
    BuilderMeta("unbreakable", 5, "NULL",
        "NULL",
        Function {
            return@Function if (it.hasItemMeta() && it.itemMeta!!.isUnbreakable) true else null
        }) {
        val data = it.first
        val builder = it.second
        val value = data["unbreakable-context"].toString()
        val unbreakable = Coerce.toBoolean(data.handle(value))
        builder.isUnbreakable = unbreakable
        return@BuilderMeta builder.isUnbreakable
    }
}

val flagsMeta by lazy {
    BuilderMeta("item-flags", 6, "NULL",
        "NULL",
        Function {
            return@Function if (it.hasItemMeta()) it.itemMeta!!.itemFlags.map { flag -> flag.name } else null
        }) {
        val data = it.first
        val builder = it.second
        val value = data["item-flags-context"] as? List<*>? ?: return@BuilderMeta builder.flags
        val list = data.handle(value.asList())
        builder.flags.clear()
        for (flagId in list) {
            val flag = Coerce.toEnum(flagId.uppercase(), ItemFlag::class.java, ItemFlag.HIDE_UNBREAKABLE) ?: continue
            builder.flags.add(flag)
        }
        return@BuilderMeta builder.flags
    }
}
val nbtMeta by lazy {
    TagMeta("nbt-keys", 7, "NULL",
        "NULL",
        Function {
            return@Function ScriptTool.itemNBTMap(
                it,
                RandomItem.configManager["config"].getStringList("options.ignore-nbt-keys")
            )
        }) {
        val data = it.first
        val tag = it.second
        println(data["nbt-keys-context"].toString())
        val context = data["nbt-keys-context"] as? Map<*, *>? ?: return@TagMeta tag
        println(context.toString())
        tag.putAll(data.handle(context).toNBT()?.asCompound() ?: return@TagMeta tag)
        return@TagMeta tag
    }
}

private val pattern = Pattern.compile("\\((?<type>byte|short|int|long|float|double|char|boolean|string)\\) ")

private fun Any.toNBT(): ItemTagData? {
    return if (this is ItemTagData) {
        this
    } else if (this is String) {
        val matcher = pattern.matcher(this)
        return if (matcher.find()) {
            val type = matcher.group("type")
            val new = this.replace(matcher.group(0), "")
            when (type) {
                "byte" -> Coerce.toByte(new).toNBT()
                "short" -> Coerce.toShort(new).toNBT()
                "int" -> Coerce.toInteger(new).toNBT()
                "long" -> Coerce.toLong(new).toNBT()
                "float" -> Coerce.toFloat(new).toNBT()
                "double" -> Coerce.toDouble(new).toNBT()
                "char" -> Coerce.toChar(new).toNBT()
                "boolean" -> Coerce.toBoolean(new).toNBT()
                else -> ItemTagData(new)
            }
        } else {
            ItemTagData(this)
        }
    } else if (this is Int) {
        ItemTagData(this)
    } else if (this is Double) {
        ItemTagData(this)
    } else if (this is Float) {
        ItemTagData(this)
    } else if (this is Short) {
        ItemTagData(this)
    } else if (this is Long) {
        ItemTagData(this)
    } else if (this is Byte) {
        ItemTagData(this)
    } else if (this is ByteArray) {
        ItemTagData(this)
    } else if (this is IntArray) {
        ItemTagData(this)
    } else if (this is List<*>) {
        ItemTagData.translateList(ItemTagList(), this)
    } else {
        val itemTag: ItemTag
        if (this is Map<*, *>) {
            itemTag = ItemTag()
            for (it in this) {
                itemTag[it.key.toString()] = (it.value ?: continue).toNBT()
            }
            itemTag
        } else if (this is ConfigurationSection) {
            itemTag = ItemTag()
            this.getValues(false).forEach { (key: String?, value: Any) ->
                itemTag[key] = value.toNBT()
            }
            itemTag
        } else {
            ItemTagData("Not supported: $this")
        }
    }
}

@Suppress("DEPRECATION")
val enchantmentMeta by lazy {
    BuilderMeta("enchantments", 8, "NULL",
        "NULL",
        Function {
            val map = HashMap<String, String>()
            it.enchantments.forEach { entry ->
                map[entry.key.name.uppercase()] = entry.value.toString()
            }
            return@Function map
        }) {
        val data = it.first
        val builder = it.second
        val value = data["enchantments-context"] as? Map<*, *>? ?: return@BuilderMeta builder.enchants
        for (entry in value.filter { entry ->
            entry.key is String && entry.value is Any
        }) {
            val optional = XEnchantment.matchXEnchantment(entry.key.toString())
            if (!optional.isPresent) {
                warning("Unknown Enchantment ${entry.key}")
                continue
            }
            val enchantment = optional.get().enchant ?: continue
            val level = Coerce.toInteger(data.handle(entry.value!!))
            if (level > 0)
                builder.enchants[enchantment] = level
        }
        return@BuilderMeta builder.enchants
    }
}
val attributesMeta by lazy {
    MetaMeta("attributes", 9, "NULL",
        "NULL",
        { itemStack ->
            if (!itemStack.hasItemMeta()) return@MetaMeta null
            val itemMeta = itemStack.itemMeta!!
            // Attributes - https://minecraft.gamepedia.com/Attribute
            if (!supports(13)) return@MetaMeta null
            val attributes = itemMeta.attributeModifiers ?: return@MetaMeta null
            val map = HashMap<String, LinkedHashMap<String, String>>()
            for ((key, modifier) in attributes.entries()) {
                val linkedMap = LinkedHashMap<String, String>()
                linkedMap["id"] = modifier.uniqueId.toString()
                linkedMap["name"] = modifier.name
                linkedMap["amount"] = modifier.amount.toString()
                linkedMap["operation"] = modifier.operation.name
                if (modifier.slot != null) linkedMap["slot"] = modifier.slot!!.name
                map[key.name.uppercase()] = linkedMap
            }
            map
        }) {
        val data = it.first
        val itemMeta = it.second
        if (supports(13)) {
            var map = data["attributes-context"] as? Map<*, *>? ?: return@MetaMeta null
            map = data.handleMap(map)
            for (attributeKey in map.keys) {
                if (attributeKey !is String) continue
                val attributeInst = Enums.getIfPresent(
                    Attribute::class.java, attributeKey.uppercase()
                ).orNull() ?: continue
                val section = map[attributeKey] as? Map<*, *>? ?: continue
                val attribId = section["id"] as? String?
                val id = if (attribId != null) UUID.fromString(attribId) else UUID.randomUUID()
                val slot = if (section["slot"] != null) Enums.getIfPresent(
                    EquipmentSlot::class.java, section["slot"].toString()
                ).or(EquipmentSlot.HAND) else null
                val modifier = AttributeModifier(
                    id,
                    section["name"].toString(),
                    section["amount"].toString().toDouble(),
                    Enums.getIfPresent(AttributeModifier.Operation::class.java, section["operation"].toString())
                        .or(AttributeModifier.Operation.ADD_NUMBER),
                    slot
                )
                itemMeta.addAttributeModifier(attributeInst, modifier)
            }
        }
        return@MetaMeta null
    }
}

@Suppress("DEPRECATION")
val colorMeta by lazy {
    BuilderMeta("color", 10, "NULL",
        "NULL",
        Function {
            if (!it.hasItemMeta() || it.itemMeta !is LeatherArmorMeta) return@Function null
            val itemMeta = it.itemMeta
            val color = (itemMeta as LeatherArmorMeta).color
            val rgb = "${color.red},${color.green},${color.blue}"
            return@Function rgb
        }) {
        val data = it.first
        val builder = it.second
        val value = data["color-context"] as? Map<*, *>? ?: return@BuilderMeta null
        val rgbStr = data.handle(value).toString().replace(" ", "")
        val splits = rgbStr.split(",")
        val red = Coerce.asInteger(splits[0])
        val green = Coerce.asInteger(splits[1])
        val blue = Coerce.asInteger(splits[2])
        if (!red.isPresent || !green.isPresent || !blue.isPresent) {
            warning("The RGB must be Integer,Integer,Integer !")
            return@BuilderMeta null
        }
        builder.color = Color.fromRGB(red.get(), green.get(), blue.get())
        return@BuilderMeta builder.color
    }
}
