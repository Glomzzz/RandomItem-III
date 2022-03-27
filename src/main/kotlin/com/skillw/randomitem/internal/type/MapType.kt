package com.skillw.randomitem.internal.type

import com.skillw.pouvoir.util.StringUtils.replacement
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.util.ListUtils.getMultiple
import taboolib.common.util.asList
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

object MapType : BaseVariableType("Map", Function {
    val data = it.first
    val context = data["${it.second}-context"] as Map<*, *>
    val args = (data["${it.second}-args"] as? Array<*> ?: arrayOf("NONE-KEY"))
    if (args.isEmpty()) return@Function "NULL"
    val keyOut = data.handle(args[0].toString())
    if (keyOut == "NONE-KEY") return@Function keyOut
    val varKey = it.second
    val thisMap: ConcurrentHashMap<*, *>
    if (!data.containsKey("$varKey-Map")) {
        thisMap = ConcurrentHashMap<String, Any>()
        for (entry in context) {
            val key = entry.key.toString()
            val value = entry.value ?: continue
            val thisKey = "this::$key"
            if (value is List<*>) {
                thisMap[thisKey] = data.handle(value.replacement(thisMap))
            } else {
                thisMap[thisKey] = data.handle(value.toString().replacement(thisMap))
            }
        }
        data["$varKey-Map"] = thisMap
    } else {
        thisMap = data["$varKey-Map"] as ConcurrentHashMap<*, *>
    }
    val value = thisMap["this::${keyOut.replacement(thisMap.mapKeys { entry -> entry.key.toString() })}"]
    val result = if (value is List<*>) {
        val list = data.handle(value.asList())
        data["${it.second}-args"] =
            if (args.size == 2) arrayOf(args[1]) else if (args.size == 3) arrayOf(args[1], args[2]) else emptyArray()
        return@Function list.getMultiple(data, it.second)
    } else data.handle(value.toString())
    return@Function result

}, false)