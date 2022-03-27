package com.skillw.randomitem.api.meta

import com.skillw.pouvoir.api.map.BaseMap
import com.skillw.pouvoir.util.FileUtils.toMap
import com.skillw.randomitem.RandomItem.metaManager
import com.skillw.randomitem.RandomItem.randomItemManager
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.variable.VariableData
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.ItemTag
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.util.ItemBuilder
import java.util.*

/**
 * 元数据
 *
 */
class MetaData : BaseMap<BaseMeta<*>, Any>(), ConfigurationSerializable {
    /**
     * 某个RItem用到的Metas的有序集合，优先级排列
     */
    private val metas: MutableList<BaseMeta<*>> by lazy {
        Collections.synchronizedList(ArrayList())
    }

    override fun put(key: BaseMeta<*>, value: Any): Any {
        metas.add(key)
        metas.sort()
        return super.put(key, value)
    }

    /**
     * 生成物品
     *
     * @param process 过程数据
     * @return
     */
    fun product(process: ProcessData): ItemStack {
        //Pre
        val itemBuilder = ItemBuilder(XMaterial.STONE)
        for (meta in metas) {
            if (meta is BuilderMeta) {
                process["${meta.key}-context"] = this[meta] ?: continue
                meta.process(itemBuilder, process)
                process.remove("${meta.key}-context")
            }
        }

        var item = itemBuilder.build()
        //Meta
        val itemMeta = item.itemMeta ?: return item
        for (meta in metas) {
            if (meta is MetaMeta) {
                process["${meta.key}-context"] = this[meta] ?: continue
                meta.process(itemMeta, process)
                process.remove("${meta.key}-context")
            }
        }
        item.itemMeta = itemMeta
        //NBT
        val itemTag = item.getItemTag()
        for (meta in metas) {
            if (meta is TagMeta) {
                process["${meta.key}-context"] = this[meta] ?: continue
                meta.process(itemTag, process)
                process.remove("${meta.key}-context")
            }
        }
        val rItemKey = itemTag["RANDOM_ITEM"].toString()
        val variableData = if (randomItemManager.containsKey(rItemKey)) {
            randomItemManager[rItemKey]!!.variableData
        } else {
            process["variableData"] as? VariableData?
        }
        if (variableData != null) {
            val compound = ItemTag()
            variableData.forEach {
                val key = it.key
                val value = process[it.key] ?: "NULL"
                compound[key] = ItemTagData.toNBT(value)
            }
            itemTag["VARIABLES_VALUE"] = compound
        }
        itemTag["RANDOM_ITEM"] = ItemTagData.toNBT(process["random-item-key"].toString())
        item = item.setItemTag(itemTag)
        return item
    }

    companion object {
        fun deserialize(section: ConfigurationSection): MetaData {
            val metaData = MetaData()
            for (it in section.getKeys(false)) {
                if (it == "key") continue
                if (it == "vars") continue
                if (it == "global-vars") continue
                val meta = metaManager[it] ?: continue
                val value = section[it] ?: continue
                metaData[meta] = if (value is ConfigurationSection) {
                    value.toMap()
                } else value
            }
            return metaData
        }

        /**
         * 从物品加载元数据
         *
         * @param itemStack 物品
         * @return
         */
        fun loadFromItem(itemStack: ItemStack): MetaData {
            val metaData = MetaData()
            for (meta in metaManager.metas) {
                val value = meta.loadData(itemStack) ?: continue
                metaData[meta] = value
            }
            return metaData
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        for (it in metas) {
            map[it.key] = this[it] ?: continue
        }
        return map
    }
}
