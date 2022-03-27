package com.skillw.randomitem.internal.manager

import com.skillw.pouvoir.util.FileUtils
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.manager.GlobalVariableManager
import com.skillw.randomitem.api.variable.Variable
import java.io.File

object GlobalVariableManagerImpl : GlobalVariableManager() {
    override val key = "GlobalVariableManager"
    override val priority: Int = 3
    override val subPouvoir = RandomItem
    private val vars by lazy {
        File(RandomItem.plugin.dataFolder, "variables")
    }

    override fun onEnable() {
        onReload()
    }

    override fun onReload() {
        clear()
        FileUtils.loadMultiply(vars, Variable::class.java).forEach {
            it.key.register()
        }
    }


}