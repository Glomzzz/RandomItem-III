package com.skillw.randomitem.api.meta

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.api.able.Registrable
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.util.FunctionUtils
import com.skillw.randomitem.util.FunctionUtils.toFunction
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common5.Coerce
import taboolib.module.nms.ItemTag
import taboolib.platform.util.ItemBuilder
import java.util.function.Function

/**
 * @author Glom_
 */

class BuilderMeta(
    override val key: String,
    override val priority: Int,
    override val buildScript: String = "NATIVE",
    override val loadScript: String = "NATIVE",
    private val loadFunction: Function<ItemStack, Any?> = Function { null },
    private val processFunction: Function<Pair<ProcessData, ItemBuilder>, Any?>,
) : BaseMeta<ItemBuilder> {
    override fun process(obj: ItemBuilder, processData: ProcessData) {
        val value = processFunction.apply(Pair(processData, obj)) ?: return
        processData["meta-$key"] = value
    }

    override fun loadData(itemStack: ItemStack): Any? {
        return loadFunction.apply(itemStack)
    }
}

class TagMeta(
    override val key: String,
    override val priority: Int,
    override val buildScript: String = "NATIVE",
    override val loadScript: String = "NATIVE",
    private val loadFunction: Function<ItemStack, Any?> = Function { null },
    private val processFunction: Function<Pair<ProcessData, ItemTag>, Any?>,
) : BaseMeta<ItemTag> {
    override fun process(obj: ItemTag, processData: ProcessData) {
        val value = processFunction.apply(Pair(processData, obj)) ?: return
        processData["meta-$key"] = value
    }

    override fun loadData(itemStack: ItemStack): Any? {
        return loadFunction.apply(itemStack)
    }

}

class MetaMeta(
    override val key: String,
    override val priority: Int,
    override val buildScript: String = "NATIVE",
    override val loadScript: String = "NATIVE",
    private val loadFunction: Function<ItemStack, Any?> = Function { null },
    private val processFunction: Function<Pair<ProcessData, ItemMeta>, Any?>,
) : BaseMeta<ItemMeta> {
    override fun process(obj: ItemMeta, processData: ProcessData) {
        val value = processFunction.apply(Pair(processData, obj)) ?: return
        processData["meta-$key"] = value
    }

    override fun loadData(itemStack: ItemStack): Any? {
        return loadFunction.apply(itemStack)
    }
}

interface BaseMeta<T> : Registrable<String>, Comparable<BaseMeta<*>>, ConfigurationSerializable {
    val priority: Int
    val loadScript: String
    val buildScript: String

    fun process(obj: T, processData: ProcessData)

    fun loadData(itemStack: ItemStack): Any?

    override fun register() {
        RandomItem.metaManager.register(key, this)
    }

    override fun compareTo(other: BaseMeta<*>): Int =
        if (this.priority == other.priority) 0 else if (this.priority > other.priority) 1 else -1

    override fun serialize(): MutableMap<String, Any> {
        val type = when (this) {
            is MetaMeta -> "meta"
            is TagMeta -> "tag"
            is BuilderMeta -> "builder"
            else -> "builder"
        }
        return mutableMapOf(
            "key" to key,
            "priority" to priority,
            "type" to type,
            "build-script" to buildScript,
            "load-script" to loadScript
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(section: ConfigurationSection): BaseMeta<*>? {
            val key = section.name
            val priority = Coerce.toInteger(section["priority"])
            val buildScript = section["build-script"].toString()
            val loadScript = section["load-script"].toString()
            return when (section["type"].toString().lowercase()) {
                "builder" -> BuilderMeta(
                    key,
                    priority,
                    buildScript,
                    loadScript,
                    toLoadFunction(loadScript),
                    toFunction(buildScript, FunctionUtils.Type.META, key)
                )
                "tag" -> TagMeta(
                    key, priority, buildScript, loadScript,
                    toLoadFunction(loadScript),
                    toFunction(buildScript, FunctionUtils.Type.META, key)
                )
                "meta" -> MetaMeta(
                    key, priority, buildScript, loadScript,
                    toLoadFunction(loadScript),
                    toFunction(buildScript, FunctionUtils.Type.META, key)
                )
                else -> null
            }
        }

        private fun toLoadFunction(string: String): Function<ItemStack, Any?> {
            if (string.isEmpty() || string.isBlank()) return Function { }
            return Function {
                return@Function Pouvoir.scriptManager.invoke(
                    string,
                    mutableMapOf("item" to it)
                )
            }
        }
    }
}