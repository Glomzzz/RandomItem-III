package com.skillw.randomitem.internal.hook.mythic

import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.item.RItem
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.module.lang.sendError
import taboolib.type.BukkitEquipment

/**
 * @ClassName : com.skillw.randomitem.listener.MMListener
 * Created by Glom_ on 2021-02-04 00:37:25
 * Copyright  2020 user. All rights reserved.
 */
object MMListener : ProxyListener {
    @SubscribeEvent(bind = "io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent")
    fun spawnIV(optional: OptionalEvent) {
        val event = optional.get<MythicMobSpawnEvent>()
        val mobType = event.mobType
        val entity = event.entity as? LivingEntity ?: return
        val equipmentList = mobType.config.getStringList("RandomItemEquipment")
        equip(equipmentList, mobType.internalName, entity)
    }

    @SubscribeEvent(bind = "io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent")
    fun deathIV(optional: OptionalEvent) {
        val event = optional.get<MythicMobDeathEvent>()
        val mobType = event.mobType
        val killer = event.killer
        if (killer !is Player) {
            return
        }
        val dropList = mobType.config.getStringList("RandomItemDrops")
        val location = event.entity.location
        drop(dropList, killer, location)
    }

    @SubscribeEvent(bind = "io.lumine.mythic.bukkit.events.MythicMobSpawnEvent")
    fun spawnV(optional: OptionalEvent) {
        val event = optional.get<io.lumine.mythic.bukkit.events.MythicMobSpawnEvent>()
        val mobType = event.mobType
        val entity = event.entity as? LivingEntity ?: return
        val equipmentList = mobType.config.getStringList("RandomItemEquipment")
        equip(equipmentList, mobType.internalName, entity)
    }

    @SubscribeEvent(bind = "io.lumine.mythic.bukkit.events.MythicMobDeathEvent")
    fun deathV(optional: OptionalEvent) {
        val event = optional.get<io.lumine.mythic.bukkit.events.MythicMobDeathEvent>()
        val mobType = event.mobType
        val killer = event.killer
        if (killer !is Player) {
            return
        }
        val dropList = mobType.config.getStringList("RandomItemDrops")
        val location = event.entity.location
        drop(dropList, killer, location)
    }

    private fun equip(equipmentList: List<String>, mobKey: String, entity: LivingEntity) {
        for (equipmentStr in equipmentList) {
            val split = equipmentStr.split(" ")
            if (split.size < 2) continue
            val itemKey = split[0]
            val rItem = RandomItem.randomItemManager[itemKey]
            if (rItem == null) {
                console().sendError("command-valid-item", itemKey)
                return
            }
            val slotKey = split[1]
            val slot = BukkitEquipment.fromString(slotKey)
            if (slot == null) {
                console().sendError("command-valid-slot", mobKey, slotKey)
                return
            }
            val demand = equipmentStr.replace("$itemKey $slotKey ", "")
            val item = RItem.handle(rItem, demand, entity)
            try {
                slot.setItem(entity, item.first.first())
            } catch (e: Exception) {
                console().sendError("command-valid-slot", mobKey, slotKey)
                return
            }
        }
    }


    private fun drop(dropList: List<String>, killer: LivingEntity, location: Location) {
        for (drop in dropList) {
            if (drop.isEmpty()) continue
            val itemKey: String
            val demandStr: String
            if (!drop.contains(" ")) {
                itemKey = drop
                demandStr = ""
            } else {
                itemKey = drop.split(" ")[0]
                demandStr = drop.replace("$itemKey ", "")
            }
            val rItem = RandomItem.randomItemManager[itemKey]
            if (rItem == null) {
                console().sendError("command-valid-item", itemKey)
                return
            }
            val pair = RItem.handle(rItem, demandStr, killer)
            pair.first.forEach {
                location.world!!.dropItem(location, it)
            }
        }
    }
}