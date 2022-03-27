package com.skillw.randomitem.api.variable.type.script

import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.util.FunctionUtils
import com.skillw.randomitem.util.FunctionUtils.toFunction
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.serialization.ConfigurationSerializable
import taboolib.common5.Coerce
import java.util.function.Function

/**
 * 脚本变量类型
 * 用于配置
 *
 * @property string 值(脚本/脚本路径)
 * @constructor
 * TODO
 *
 * @param key 唯一标识符
 * @param cache 是否缓存到过程数据
 * @param function 函数
 */
class ScriptVariableType(
    key: String,
    val string: String,
    cache: Boolean,
    function: Function<Pair<ProcessData, String>, Any>
) :
    BaseVariableType(
        key,
        function,
        cache
    ), ConfigurationSerializable {


    companion object {
        @JvmStatic
        fun deserialize(section: ConfigurationSection): ScriptVariableType? {
            val key = section.name
            val script = section["script"] as? String? ?: return null
            val cache = Coerce.toBoolean(section["cache"])
            return ScriptVariableType(key, script, cache) {
                val func = toFunction<Pair<ProcessData, Any?>, Any?>(script, FunctionUtils.Type.TYPE, it.second)
                return@ScriptVariableType func.apply(Pair(it.first, null)) ?: "NULL"
            }
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf("key" to key, "script" to string)
    }


}