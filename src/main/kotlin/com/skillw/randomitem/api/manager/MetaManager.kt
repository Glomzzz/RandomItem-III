package com.skillw.randomitem.api.manager

import com.skillw.pouvoir.api.manager.Manager
import com.skillw.pouvoir.api.map.LowerMap
import com.skillw.randomitem.api.meta.BaseMeta
import java.util.*

/**
 * Meta管理器
 */
abstract class MetaManager : Manager, LowerMap<BaseMeta<*>>() {
    /**
     * 有序Metas，根据优先级排列
     */
    val metas: MutableList<BaseMeta<*>> by lazy {
        Collections.synchronizedList(ArrayList())
    }
}