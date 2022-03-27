package com.skillw.randomitem.api.data

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.api.map.BaseMap
import com.skillw.pouvoir.util.MessageUtils.wrong
import com.skillw.pouvoir.util.StringUtils.placeholder
import com.skillw.pouvoir.util.StringUtils.toArgs
import com.skillw.randomitem.api.variable.VariableData
import org.bukkit.entity.LivingEntity
import taboolib.common.util.asList
import taboolib.common5.Coerce
import taboolib.module.chat.colored
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 过程数据
 *
 * @constructor
 *
 * @param entity 实体
 */
class ProcessData(entity: LivingEntity? = null) : BaseMap<String, Any>() {

    companion object {
        @JvmStatic
        fun pointData(entity: LivingEntity?, string: String): ProcessData {
            val processData = ProcessData(entity)
            if (string.length <= 2) return processData
            try {
                val array = string.let { it.substring(1, it.lastIndex) }.toArgs()
                for (single in array) {
                    if (single.isEmpty() || single.isBlank()) continue
                    val key = single.split("=")[0]
                    val value = single.replace("$key=", "")
                    processData[key] = if (entity != null) value.placeholder(entity) else value
                }
            } catch (e: Exception) {
                wrong("Wrong Point Data Format!")
            }
            return processData
        }
    }

    init {
        if (entity != null)
            this["entity"] = entity
    }

    val data = map

    fun <K, V> handleMap(map: Map<K, V>): Map<String, Any> {
        val newMap = ConcurrentHashMap<String, Any>()
        map.forEach { entry ->
            newMap[handle(entry.key.toString())] = handle(entry.value!!)
        }
        return newMap
    }

    /**
     * 解析Any
     *
     * @param any 字符串/字符串集合/Map
     * @return
     */
    fun handle(any: Any): Any {
        if (any is String) {
            return handle(any)
        }
        if (any is List<*>) {
            if (any.isEmpty()) return "[]"
            if (any[0] is Map<*, *>) {
                val mapList = Coerce.toListOf(any, Map::class.java)
                val newList = LinkedList<Map<*, *>>()
                mapList.forEach {
                    newList.add(handleMap(it))
                }
                return newList
            }
            return handle(any.asList())
        }
        if (any is Map<*, *>) {
            return handleMap(any)
        }
        return any
    }

    fun handle(string: String): String {
        val entity = this["entity"] as? LivingEntity?
        var temp = string
        this.filter {
            it.value is String
        }.forEach {
            temp = temp.replace("<${it.key}>()", it.value.toString())
        }
        val variableData =
            (this["variableData"] as? VariableData?) ?: return Pouvoir.pouPlaceHolderAPI.replace(entity, temp).colored()
        if (variableData.containsVariable(temp)) {
            temp = variableData.replace(temp, this)
        }
        return Pouvoir.pouPlaceHolderAPI.replace(entity, temp).colored()
    }

    fun handle(strings: Collection<String>): List<String> {
        val list = LinkedList<String>()
        strings.forEach {
            list.add(handle(it))
        }
        return list
    }
}