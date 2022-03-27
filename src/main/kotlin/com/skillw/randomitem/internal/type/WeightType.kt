package com.skillw.randomitem.internal.type

import com.skillw.pouvoir.util.StringUtils.toList
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.util.ListUtils.getMultiple
import taboolib.common5.Coerce
import taboolib.common5.RandomList
import java.util.function.Function
import java.util.regex.Pattern

val pattern: Pattern =
    Pattern.compile("(?<weight>([+\\-])?(\\d+(?:(\\.\\d+(%)?)|%)?)(-)?(\\d+(?:(\\.\\d+(%)?)|%)?)?)::(?<value>.*)")

object WeightType : BaseVariableType("weight", Function {
    val data = it.first
    val context = data["${it.second}-context"] as Map<*, *>
    val list = data.handle(Coerce.toListOf(context["values"], String::class.java))
    val weightRandom = RandomList<String>()
    for (str in list) {
        val matcher = pattern.matcher(str)
        if (matcher.find()) {
            val weight = Coerce.toInteger(matcher.group("weight"))
            val value = Coerce.toString(matcher.group("value"))
            weightRandom.add(value, weight)
        }
    }
    val result = weightRandom.random()!!
    if (result.contains("\n")) {
        val strList = result.toList()
        return@Function strList.getMultiple(data, it.second)
    } else
        return@Function result

})