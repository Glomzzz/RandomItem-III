package com.skillw.randomitem.api.variable

import com.skillw.pouvoir.api.able.Registrable
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.RandomItem.variableTypeManager
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.variable.type.BaseVariableType
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerializable
import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.module.lang.sendLang

/**
 * 变量
 */
class Variable(override val key: String, val type: BaseVariableType, val cache: Boolean = true) : Registrable<String>,
    ConfigurationSerializable {
    /**
     * 变量数据
     */
    val data = LinkedHashMap<String, Any>()

    companion object {
        @JvmStatic
        fun deserialize(section: ConfigurationSection): Variable? {
            val key = section.name
            val typeKey = section["type"].toString()
            val temp = variableTypeManager[typeKey]
            val type = if (temp != null) temp
            else {
                console().sendLang("command-valid-type", typeKey)
                return null
            }
            val cache = Coerce.toBoolean(section["cache"]?.toString() ?: "true")
            val variable = Variable(key, type, cache)
            variable.data["key"] = key
            section.getKeys(false).forEach {
                variable.data[it] = section[it]!!
            }
            return variable
        }
    }

    /**
     * 根据过程数据与参数解析
     * 变量数据会以 "key-context" 为键存入 过程数据
     * 参数会以 "key-args" 为键存入 过程数据
     *
     * @param process 过程数据
     * @param args 参数
     * @return
     */
    fun analysis(process: ProcessData, vararg args: Any): String {
        if (process.containsKey(key)) {
            return process[key].toString()
        }
        process["$key-context"] = data
        process["$key-args"] =
            if (args.isEmpty() ||
                args[0].toString().isEmpty() ||
                args[0].toString().isBlank() ||
                args[0].toString() == ""
            ) emptyArray() else args
        val result = type.analysis(process, key).toString()
        process.remove("$key-context")
        process.remove("$key-args")
        if (type.cache && this.cache)
            process[key] = result
        return result
    }


    /**
     * 注册到全局数据管理器
     *
     */
    override fun register() {
        RandomItem.globalVariableManager.register(this)
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = linkedMapOf<String, Any>("key" to key, "type" to type.key)
        map.putAll(data)
        return map
    }
}