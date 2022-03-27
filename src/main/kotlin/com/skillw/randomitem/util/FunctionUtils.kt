package com.skillw.randomitem.util

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.util.StringUtils.toList
import com.skillw.pouvoir.util.StringUtils.toStringWithNext
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.util.ListUtils.getMultiple
import taboolib.common.util.asList
import java.util.function.Function

object FunctionUtils {

    enum class Type {
        TYPE, META
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : Pair<ProcessData, Any?>, R> toFunction(string: String, type: Type, key: String): Function<T, R> {
        fun map(it: T): MutableMap<String, Any> {
            val map = mutableMapOf(
                "data" to it.first,
                "context" to when (type) {
                    Type.TYPE -> it.first["$key-context"]!!
                    Type.META -> it.first["$key-context"]!!
                },
                "toStr" to Function<Any, String> { it1 -> it1.toStringWithNext() },
                "toList" to Function<String, List<String>> { it1 -> it1.toList() },
                "getMultiple" to Function<Any, String> { it1 ->
                    val list: List<String> = if (it1 is List<*>) {
                        it1.asList()
                    } else {
                        it1.toString().toList()
                    }
                    list.getMultiple(it.first, key)
                }
            )
            if (it.second != null) {
                map["obj"] = it.second!!
            }
            return map
        }
        return Function<T, R> {
            return@Function (Pouvoir.scriptManager.invoke(string, map(it)) ?: "NULL") as R
        }
    }
}