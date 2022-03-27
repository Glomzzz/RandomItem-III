package com.skillw.randomitem.internal.manager

import com.skillw.pouvoir.util.FileUtils
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.manager.MetaManager
import com.skillw.randomitem.api.meta.BaseMeta
import com.skillw.randomitem.internal.meta.*
import java.io.File

object MetaManagerImpl : MetaManager() {
    override val key = "MetaManager"
    override val priority: Int = 1
    override val subPouvoir = RandomItem

    override fun register(key: String, value: BaseMeta<*>) {
        metas.add(value)
        metas.sort()
        super.register(key, value)
    }

    private val metasFile by lazy {
        File(RandomItem.plugin.dataFolder, "meta")
    }

    override fun onEnable() {
        onReload()
    }

    override fun onReload() {
        this.clear()
        displayMeta.register()
        materialMeta.register()
        damageMeta.register()
        dataMeta.register()
        loreMeta.register()
        unbreakableMeta.register()
        flagsMeta.register()
        nbtMeta.register()
        enchantmentMeta.register()
        attributesMeta.register()
        colorMeta.register()
        FileUtils.loadMultiply(metasFile, BaseMeta::class.java).forEach {
            it.key.register()
        }
    }


}