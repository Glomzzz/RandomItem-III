package com.skillw.randomitem

import com.skillw.pouvoir.api.annotation.PManager
import com.skillw.pouvoir.api.manager.ManagerData
import com.skillw.pouvoir.api.plugin.SubPouvoir
import com.skillw.pouvoir.api.thread.BasicThreadFactory
import com.skillw.pouvoir.util.FileUtils
import com.skillw.pouvoir.util.MessageUtils
import com.skillw.pouvoir.util.Pair
import com.skillw.randomitem.api.manager.GlobalVariableManager
import com.skillw.randomitem.api.manager.MetaManager
import com.skillw.randomitem.api.manager.RandomItemManager
import com.skillw.randomitem.api.manager.VariableTypeManager
import com.skillw.randomitem.internal.manager.RIConfig
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.BukkitPlugin
import java.io.File
import java.util.concurrent.ScheduledThreadPoolExecutor

object RandomItem : Plugin(), SubPouvoir {
    override fun getConfigs(): MutableMap<String, Pair<File, YamlConfiguration>> {
        return mutableMapOf("config" to Pair(config.file!!, FileUtils.loadConfigFile(config.file!!)!!))
    }

    /**
     * Basic
     */

    override val key = "RandomItem"
    override val plugin: JavaPlugin by lazy {
        BukkitPlugin.getInstance()
    }
    override val poolExecutor: ScheduledThreadPoolExecutor by lazy {
        ScheduledThreadPoolExecutor(
            20,
            BasicThreadFactory.Builder().daemon(true).namingPattern("random-item-schedule-pool-%d").build()
        )
    }

    /**
     * Config
     */
    @Config("config.yml")
    lateinit var config: ConfigFile

    /**
     * Managers
     */

    override lateinit var managerData: ManagerData

    @JvmStatic
    @PManager
    lateinit var configManager: RIConfig

    @JvmStatic
    @PManager
    lateinit var metaManager: MetaManager

    @JvmStatic
    @PManager
    lateinit var variableTypeManager: VariableTypeManager


    @JvmStatic
    @PManager
    lateinit var randomItemManager: RandomItemManager

    @JvmStatic
    @PManager
    lateinit var globalVariableManager: GlobalVariableManager

    override fun onLoad() {
        load()
        MessageUtils.info("[&bRandomItem&d] &eRandomItem is loading...")
    }

    override fun onEnable() {
        enable()
        MessageUtils.info("[&bRandomItem&d] &aRandomItem is enabling...")
    }

    override fun onActive() {
        active()
    }

    override fun onDisable() {
        disable()
        MessageUtils.info("[&bRandomItem&d] &cRandomItem is enabling...")
    }


}