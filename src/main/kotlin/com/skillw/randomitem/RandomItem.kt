package com.skillw.randomitem

import com.skillw.pouvoir.api.annotation.PouManager
import com.skillw.pouvoir.api.manager.ManagerData
import com.skillw.pouvoir.api.plugin.SubPouvoir
import com.skillw.pouvoir.api.thread.BasicThreadFactory
import com.skillw.randomitem.api.manager.GlobalVariableManager
import com.skillw.randomitem.api.manager.MetaManager
import com.skillw.randomitem.api.manager.RandomItemManager
import com.skillw.randomitem.api.manager.VariableTypeManager
import com.skillw.randomitem.internal.manager.RIConfig
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.BukkitPlugin
import java.util.concurrent.ScheduledThreadPoolExecutor

object RandomItem : Plugin(), SubPouvoir {

    /**
     * Basic
     */

    override val key = "RandomItem"
    override val plugin: JavaPlugin by lazy {
        BukkitPlugin.getInstance()
    }
    val poolExecutor: ScheduledThreadPoolExecutor by lazy {
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
    @PouManager
    lateinit var configManager: RIConfig

    @JvmStatic
    @PouManager
    lateinit var metaManager: MetaManager

    @JvmStatic
    @PouManager
    lateinit var variableTypeManager: VariableTypeManager


    @JvmStatic
    @PouManager
    lateinit var randomItemManager: RandomItemManager

    @JvmStatic
    @PouManager
    lateinit var globalVariableManager: GlobalVariableManager

    override fun onLoad() {
        load()
    }

    override fun onEnable() {
        enable()
    }

    override fun onActive() {
        active()
    }

    override fun onDisable() {
        disable()
    }


}