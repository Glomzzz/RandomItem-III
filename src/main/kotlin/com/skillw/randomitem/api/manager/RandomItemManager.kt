package com.skillw.randomitem.api.manager

import com.skillw.pouvoir.api.manager.Manager
import com.skillw.pouvoir.api.map.BaseMap
import com.skillw.pouvoir.api.map.LowerMap
import com.skillw.randomitem.api.item.RItem
import java.io.File

/**
 * 随机物品管理器
 *
 */
abstract class RandomItemManager : Manager, LowerMap<RItem>() {
    /**
     * RItem to File map
     */
    val fileMap = BaseMap<RItem, File>()
}