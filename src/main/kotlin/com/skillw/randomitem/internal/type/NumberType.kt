package com.skillw.randomitem.internal.type

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.util.NumberUtils
import com.skillw.randomitem.api.variable.type.BaseVariableType
import taboolib.common5.Coerce
import java.util.function.Function

object NumberType : BaseVariableType("Number", Function {
    val data = it.first
    val context = data["${it.second}-context"] as Map<*, *>
    val min = Coerce.asDouble(data.handle(context["min"] ?: context["start"] ?: "no"))
    val max = Coerce.asDouble(data.handle(context["max"] ?: context["bound"] ?: "no"))
    val format = context["format"]?.toString() ?: Pouvoir.configManager.numberFormat
    if (!min.isPresent || !max.isPresent) {
        return@Function "Wrong-Number"
    }
    return@Function NumberUtils.random(min.get(), max.get(), format)

})