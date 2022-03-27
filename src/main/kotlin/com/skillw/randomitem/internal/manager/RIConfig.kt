package com.skillw.randomitem.internal.manager

import com.skillw.pouvoir.Pouvoir
import com.skillw.pouvoir.api.manager.ConfigManager
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.RandomItem.plugin
import taboolib.common.platform.Platform
import taboolib.module.metrics.Metrics
import taboolib.module.metrics.charts.SingleLineChart
import java.io.File


object RIConfig : ConfigManager(RandomItem) {
    override val priority = 0
    override fun defaultOptions(): Map<String, Map<String, Any>> = mapOf(
        "config" to mapOf(
            "options" to mapOf(
                "debug" to false,
                "check-version" to true,
                "ignore-nbt-keys" to listOf(
                    "VARIABLES_DATA", "HideFlags", "Enchantments", "display", "Damage", "AttributeModifiers", "ench"
                ),
                "list" to mapOf(
                    "pre-page-size" to 10,
                    "up" to "&d&l&m=======================================",
                    "format" to "&a{order} &b -> &6{key} &5,&b{name}",
                    "left" to "  &b<-",
                    "page-info" to "      &e{current}&5/&e{total}      ",
                    "right" to "&b->  ",
                    "down" to "&d&l&m======================================="
                )
            )
        )
    )

    override fun onLoad() {
        createIfNotExists("items", "ExampleItem.yml")
        createIfNotExists("variables", "Script.yml")
        createIfNotExists("type", "default.yml")
        createIfNotExists("scripts")
        createIfNotExists("meta")
        Pouvoir.scriptManager.addDir(File(plugin.dataFolder, "scripts"), RandomItem)
        val metrics = Metrics(14179, RandomItem.plugin.description.version, Platform.BUKKIT)
        metrics.addCustomChart(SingleLineChart("items") {
            RandomItem.randomItemManager.size
        })
        metrics.addCustomChart(SingleLineChart("global-vars") {
            RandomItem.globalVariableManager.size
        })
        metrics.addCustomChart(SingleLineChart("var-types") {
            RandomItem.variableTypeManager.size
        })
        metrics.addCustomChart(SingleLineChart("metas") {
            RandomItem.metaManager.size
        })
    }

}