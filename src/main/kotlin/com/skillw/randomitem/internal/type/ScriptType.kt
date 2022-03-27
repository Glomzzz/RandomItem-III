package com.skillw.randomitem.internal.type

import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.util.FunctionUtils
import java.util.function.Function

object ScriptType : BaseVariableType("script", Function {
    val data = it.first
    val context = data["${it.second}-context"] as Map<*, *>
    val script = data.handle(context["script"].toString())
    val func = FunctionUtils.toFunction<Pair<ProcessData, Any?>, Any?>(script, FunctionUtils.Type.TYPE, it.second)
    return@Function func.apply(Pair(data, null)) ?: "NULL"
})