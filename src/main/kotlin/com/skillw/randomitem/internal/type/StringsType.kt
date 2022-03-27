package com.skillw.randomitem.internal.type

import com.skillw.pouvoir.util.StringUtils.toList
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.util.ListUtils.getMultiple
import taboolib.common.util.asList
import java.util.function.Function

object StringsType : BaseVariableType("strings", Function {
    val data = it.first
    val context = data["${it.second}-context"] as Map<*, *>
    val list = data.handle(context["values"]?.asList() ?: return@Function "NULL")
    if (list.size == 1) {
        return@Function list[0].toList().getMultiple(data, it.second)
    }
    return@Function list.getMultiple(data, it.second)
})