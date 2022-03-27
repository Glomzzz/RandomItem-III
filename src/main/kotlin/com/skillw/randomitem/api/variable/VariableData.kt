package com.skillw.randomitem.api.variable

import com.skillw.pouvoir.api.map.KeyMap
import com.skillw.pouvoir.util.StringUtils.toArgs
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.data.ProcessData
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.ItemTagData
import taboolib.module.nms.getItemTag
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * 变量数据
 *
 */
class VariableData() : KeyMap<String, Variable>(), ConfigurationSerializable {
    private var varPattern: Pattern = Pattern.compile("<(null)>\\((.*?)\\)")
    private val regexCache = StringBuilder()


    override fun put(key: String, value: Variable): Variable {
        map[key] = value
        if (regexCache.isNotEmpty()) {
            regexCache.append("|")
        }
        regexCache.append(key)
        val regex = "<($regexCache)>\\(([^()]*)\\)"
        varPattern = Pattern.compile(regex, 2)
        return value
    }

    companion object {
        fun deserialize(section: org.bukkit.configuration.ConfigurationSection): VariableData {
            val variableData = VariableData()
            for (it in section.getKeys(false)) {
                variableData[it] = Variable.deserialize(section.getConfigurationSection(it)!!) ?: continue
            }
            return variableData
        }

        /**
         * 从物品加载变量数据
         *
         * @param itemStack 物品
         * @return
         */
        fun loadFromItem(itemStack: ItemStack): VariableData {
            val variableData = VariableData()
            val tag = itemStack.getItemTag()
            if (tag.containsKey("VARIABLES_DATA")) {
                for (it in ItemTagData.toNBT(tag["VARIABLES_DATA"]).asCompound()) {
                    val variable = RandomItem.globalVariableManager[it.key] ?: continue
                    variableData.register(variable)
                }
            }
            return variableData
        }
    }

    fun containsVariable(str: String): Boolean {
        val matcher = varPattern.matcher(str)
        return matcher.find()
    }


    private fun replaceVar(result: String, processData: ProcessData): String {
        var temp = result
        val matcher = varPattern.matcher(temp)
        if (!matcher.find()) return temp
        val stringBuffer = StringBuffer()
        do {
            val variable = map[matcher.group(1)] ?: continue
            val args = matcher.group(2)
            val replaced = variable.analysis(processData, *args.toArgs())
            matcher.appendReplacement(stringBuffer, replaced)
        } while (matcher.find())
        temp = matcher.appendTail(stringBuffer).toString()
        return temp
    }

    /**
     * 解析并替换字符串中的变量
     *
     * @param string 字符串
     * @param processData 过程数据
     * @return
     */
    fun replace(string: String, processData: ProcessData): String {
        val result = replaceVar(string, processData)
        return processData.handle(result)
    }

    override fun serialize(): MutableMap<String, Any> {
        val map = ConcurrentHashMap<String, Any>()
        this.map.forEach {
            map[it.key] = it.value.serialize()
        }
        return map
    }
}