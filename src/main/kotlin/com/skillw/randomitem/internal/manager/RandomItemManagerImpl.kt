package com.skillw.randomitem.internal.manager

import com.skillw.pouvoir.util.FileUtils
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.item.RItem
import com.skillw.randomitem.api.manager.RandomItemManager
import java.io.File

object RandomItemManagerImpl : RandomItemManager() {
    override val key = "RandomItemManager"
    override val priority: Int = 4
    override val subPouvoir = RandomItem
    private val items by lazy {
        File(RandomItem.plugin.dataFolder, "items")
    }

    override fun onEnable() {
        onReload()
    }

    override fun onReload() {
        clear()
        FileUtils.loadMultiply(items, RItem::class.java).forEach {
            it.key.register()
            fileMap[it.key] = it.value
        }
    }
}