package com.skillw.randomitem.internal.type

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.util.CalculationUtils.calculateDouble
import com.skillw.pouvoir.util.NumberUtils.format
import com.skillw.randomitem.api.variable.type.BaseVariableType
import org.bukkit.entity.LivingEntity
import taboolib.common5.Coerce
import java.util.function.Function

object CalculationType : BaseVariableType("Calculation", Function {
    val data = it.first
    val entity = data["entity"] as? LivingEntity?
    val context = data["${it.second}-context"] as Map<*, *>
    val formula = context["formula"].toString()
    val min = Coerce.asDouble(data.handle(context["min"] ?: "no"))
    val max = Coerce.asDouble(data.handle(context["max"] ?: "no"))
    val value = data.handle(formula).calculateDouble(entity)
    val result =
        if (min.isPresent && value <= min.get()) min.get() else if (max.isPresent && value >= max.get()) max.get() else value
    val format = context["format"] as? String ?: Pouvoir.configManager.numberFormat
    return@Function result.format(format)
})