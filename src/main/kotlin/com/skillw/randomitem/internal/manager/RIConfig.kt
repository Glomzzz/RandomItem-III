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

    override fun onLoad() {
        createIfNotExists("items", "ExampleItem.yml")
        createIfNotExists("variables", "Script.yml")
        createIfNotExists("type", "default.yml")
        createIfNotExists("scripts")
        createIfNotExists("meta")
        Pouvoir.scriptManager.addScriptDir(File(plugin.dataFolder, "scripts"))
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