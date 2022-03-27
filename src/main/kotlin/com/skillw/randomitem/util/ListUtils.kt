package com.skillw.randomitem.util

import com.skillw.pouvoir.util.StringUtils.toStringWithNext
import com.skillw.randomitem.api.data.ProcessData
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

object ListUtils {
    @JvmStatic
    fun List<String>.getMultiple(processData: ProcessData, key: String): String {
        val strList = this
        val args = processData["$key-args"] as Array<*>
        if (args.isEmpty()) return processData.handle(strList.toStringWithNext())
        val indexes: IntArray = when (args.size) {
            1 -> intArrayOf(
                args[0].toString().toInt()
            )
            2 -> intArrayOf(
                args[0].toString().toInt(),
                args[1].toString().toInt()
            )
            else -> intArrayOf()
        }
        val result = try {
            if (indexes.isEmpty()) {
                processData.handle(strList.toStringWithNext())
            } else if (indexes.size == 1) {
                processData.handle(strList[indexes[0]])
            } else if (indexes.size == 2) {
                val arrayList = ArrayList<String>()
                for (i in indexes[0]..indexes[1]) {
                    arrayList.add(processData.handle(strList[i]))
                }
                arrayList.toStringWithNext()
            } else {
                "NULL"
            }
        } catch (e: Exception) {
            console().sendLang("call-valid-index", e.cause.toString())
            "NULL"
        }
        return result
    }

    @JvmStatic
    fun List<String>.nextLineToNew(): List<String> {
        val list = java.util.ArrayList<String>()
        for (str in this) {
            if (!str.contains("\n")) list.add(str)
            else str.split("\n").filter { it.isNotBlank() && it.isNotEmpty() }.forEach { list.add(it) }
        }
        return list
    }
}