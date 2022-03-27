package com.skillw.randomitem.internal.manager

import com.skillw.pouvoir.util.FileUtils
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.manager.VariableTypeManager
import com.skillw.randomitem.api.variable.type.BaseVariableType
import com.skillw.randomitem.api.variable.type.script.ScriptVariableType
import com.skillw.randomitem.internal.type.*
import java.io.File
import java.util.function.Function

object VariableTypeManagerImpl : VariableTypeManager() {
    override val key = "SectionTypeManager"
    override val priority: Int = 2
    override val subPouvoir = RandomItem
    private val types by lazy {
        File(RandomItem.plugin.dataFolder, "type")
    }

    override fun onEnable() {
        onReload()
    }

    override fun onReload() {
        clear()
        map["none"] = object : BaseVariableType(key, Function { }) {}
        CalculationType.register()
        MapType.register()
        NumberType.register()
        StringsType.register()
        WeightType.register()
        ScriptType.register()
        FileUtils.loadMultiply(types, ScriptVariableType::class.java).forEach {
            it.key.register()
        }

    }


}