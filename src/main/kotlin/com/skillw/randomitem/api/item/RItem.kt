package com.skillw.randomitem.api.item

import com.skillw.pouvoir.api.able.Registrable
import com.skillw.pouvoir.taboolib.module.nms.ItemTagData
import com.skillw.pouvoir.util.CalculationUtils.calculate
import com.skillw.pouvoir.util.CalculationUtils.calculateDouble
import com.skillw.pouvoir.util.FileUtils.loadYaml
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.RandomItem.randomItemManager
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.meta.MetaData
import com.skillw.randomitem.api.variable.VariableData
import com.skillw.randomitem.util.mirrorFutureA
import com.skillw.randomitem.util.mirrorNowA
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.random
import taboolib.common5.Coerce
import taboolib.common5.Demand
import taboolib.common5.mirrorNow
import taboolib.module.nms.getItemTag
import taboolib.platform.util.giveItem

class RItem(override val key: String, val metaData: MetaData, val variableData: VariableData) : Registrable<String>,
    ConfigurationSerializable {
    /**
     * 全局数据
     */
    val globalVariables = HashSet<String>()

    /**
     * 注册RItem
     *
     */
    override fun register() {
        randomItemManager.register(key, this)
    }

    /**
     * 注销RItem
     */
    fun unRegister() {
        RandomItem.poolExecutor.execute {
            RandomItem.randomItemManager.remove(key)
            val file = randomItemManager.fileMap[this]
            val config = file?.loadYaml() ?: return@execute
            config.set(key, null)
            config.save(file)
            randomItemManager.fileMap.remove(this)
        }
    }

    /**
     * 序列化
     *
     * @return MutableMap<String, Any>
     */
    override fun serialize(): MutableMap<String, Any> {
        val map = LinkedHashMap<String, Any>()
        for (it in metaData.serialize()) {
            map[it.key] = it.value
        }
        if (globalVariables.isNotEmpty()) {
            map["global-vars"] = globalVariables.toList()
        }
        if (variableData.isNotEmpty()) {
            map["vars"] = variableData.serialize()
        }
        return map
    }

    companion object {
        /**
         * 反序列化
         *
         * @param section 节点
         * @return RItem
         */
        @JvmStatic
        fun deserialize(section: org.bukkit.configuration.ConfigurationSection): RItem {
            val rItem =
                if (section.contains("vars"))
                    RItem(
                        section.name,
                        MetaData.deserialize(section),
                        VariableData.deserialize(section.getConfigurationSection("vars")!!)
                    )
                else
                    RItem(
                        section.name,
                        MetaData.deserialize(section),
                        VariableData()
                    )
            if (section.contains("global-vars")) {
                val list = Coerce.toListOf(section["global-vars"], String::class.java)
                rItem.globalVariables.addAll(list)
                for (it in list) {
                    val variable = RandomItem.globalVariableManager[it] ?: continue
                    rItem.variableData.register(variable)
                }
            }
            return rItem
        }

        /**
         * 从物品实例上加载
         *
         * @param key 唯一标识符
         * @param itemStack 物品
         * @return
         */
        @JvmStatic
        fun createFromItem(key: String, itemStack: ItemStack): RItem {
            val tag = itemStack.getItemTag()
            val rItem = mirrorFutureA<RItem>("create-form-item") {
                RItem(
                    key,
                    MetaData.loadFromItem(itemStack),
                    VariableData()
                )
            }.get()
            if (tag.containsKey("VARIABLES_DATA")) {
                for (it in ItemTagData.toNBT(tag["VARIABLES_DATA"]).asCompound()) {
                    if (RandomItem.globalVariableManager.containsKey(it.key))
                        rItem.globalVariables.add(it.key)
                }
            }
            return rItem
        }

        /**
         * 解析参数
         *
         * @param rItem RItem实例
         * @param demandStr 参数字符串
         * @param proxyPlayer 玩家
         * @return
         */
        @JvmStatic
        fun handle(
            rItem: RItem,
            demandStr: String,
            livingEntity: LivingEntity?
        ): Pair<List<ItemStack>, Boolean> {
            return mirrorNowA("product-with-pointing-data") {
                val demand = Demand("dem $demandStr")
                val amountFormula = demand.get("amount", "1")!!
                val probableFormula = demand.get("probable", "1")!!
                val data = demand.get("data", "[]")!!
                val isSame = demand.tags.contains("same")
                val temp = Coerce.toInteger(amountFormula.calculate(livingEntity))
                val amount = if (temp == 0) 1 else temp
                val probable = probableFormula.calculateDouble(livingEntity)
                if (!random(probable)) {
                    return@mirrorNowA Pair(emptyList(), false)
                }
                val processData = ProcessData.pointData(livingEntity, data)
                if (isSame) {
                    val item = rItem.product(livingEntity, processData)
                    return@mirrorNowA Pair(Array(amount) { item }.asList(), true)
                }
                Pair(Array(amount) { rItem.product(livingEntity, processData) }.asList(), false)
            }
        }
    }

    /**
     * 生成物品（无实体数据）
     * 一般用于展览
     * @return
     */
    fun product(): ItemStack {
        return mirrorNow("product-item-without-entity") { metaData.product(ProcessData()) }
    }

    /**
     * 根据实体，过程数据生成物品
     * 用于正常使用
     *
     * @param entity 实体
     * @param processData 过程数据
     * @return
     */

    fun product(entity: LivingEntity?, processData: ProcessData = ProcessData(entity)): ItemStack {
        processData["variableData"] = variableData
        processData["random-item-key"] = key
        return mirrorNow("product-item-with-entity") { metaData.product(processData) }
    }

    /**
     * 根据玩家与过程数据生成物品，并给予此玩家
     *
     * @param player 玩家
     * @param processData 过程数据
     * @return
     */
    fun give(player: Player, processData: ProcessData = ProcessData(player)): ItemStack {
        val item = product(player, processData)
        player.giveItem(listOf(item))
        return item
    }

    internal fun give(player: ProxyPlayer, processData: ProcessData = ProcessData(player.cast<Player>())): ItemStack {
        return give(player.cast<Player>(), processData)
    }

}