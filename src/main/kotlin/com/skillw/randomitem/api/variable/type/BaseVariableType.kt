package com.skillw.randomitem.api.variable.type

import com.skillw.pouvoir.api.able.Keyable
import com.skillw.randomitem.RandomItem.variableTypeManager
import com.skillw.randomitem.api.data.ProcessData
import java.util.function.Function

/**
 * @author Glom_
 */
abstract class BaseVariableType(override val key: String, val cache: Boolean = true) :
    Keyable<String> {
    protected var function: Function<Pair<ProcessData, String>, Any> = Function { }

    constructor(key: String, function: Function<Pair<ProcessData, String>, Any>, cache: Boolean = true) : this(
        key,
        cache
    ) {
        this.function = function
    }

    /**
     * 根据processData中的context调用解析函数
     *
     * @param processData
     * @return
     */
    fun analysis(processData: ProcessData, key: String): Any {
        return function.apply(Pair(processData, key))
    }

    override fun register() {
        variableTypeManager.register(key, this)
    }
}