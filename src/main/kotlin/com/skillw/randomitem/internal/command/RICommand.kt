package com.skillw.randomitem.internal.command

import com.skillw.pouvoir.util.StringUtils.replacement
import com.skillw.pouvoir.util.StringUtils.toArgs
import com.skillw.randomitem.RandomItem
import com.skillw.randomitem.api.data.ProcessData
import com.skillw.randomitem.api.item.RItem
import com.skillw.randomitem.api.item.RItem.Companion.handle
import com.skillw.randomitem.internal.meta.displayMeta
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.io.newFile
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.common.util.sync
import taboolib.common5.Coerce
import taboolib.common5.Demand
import taboolib.common5.Mirror
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.lang.sendLang
import taboolib.module.nms.*
import taboolib.platform.util.giveItem
import taboolib.platform.util.hoverItem
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@CommandHeader(name = "randomitem", aliases = ["ri"])
object RICommand {


    @CommandBody
    val help = subCommand {
        execute<ProxyCommandSender> { sender, context, argument ->
            if (!sender.hasPermission("ri.command.help")) {
                sender.sendLang("command-no-permission")
                return@execute
            }
            sender.sendLang("command-info")
        }
    }

    @CommandBody
    val main = mainCommand {
        incorrectSender { sender, _ ->
            sender.sendLang("command-only-player")
        }
        incorrectCommand { sender, context, index, state ->
            sender.sendLang("command-valid-command")
        }
        execute<ProxyCommandSender> { sender, context, argument ->
            if (!sender.hasPermission("ri.command.help")) {
                sender.sendLang("command-no-permission")
                return@execute
            }
            sender.sendLang("command-info")
        }

    }

    @CommandBody
    val report = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            Mirror.report(sender)
        }
    }

    @CommandBody
    val clear = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            sender.sendLang("command-clear")
            Mirror.mirrorData.clear()
        }
    }

    private fun handleAndGive(
        rItem: RItem,
        argument: String,
        sender: ProxyCommandSender,
        proxyPlayer: ProxyPlayer?
    ) {
        val pair = handle(rItem, argument.replace("${rItem.key} ", ""), proxyPlayer?.cast<Player>())
        val list = pair.first
        val isSame = pair.second
        val player = proxyPlayer?.cast<Player>() ?: sender.cast()
        if (list.isEmpty()) return
        if (!isSame) {
            for (item in list) {
                player.giveItem(item)
                sender.sendLang("command-give-item", player.displayName, "1", item.getName())
            }
            return
        }
        player.giveItem(list)
        sender.sendLang("command-give-item", player.displayName, "${list.size}", list[0].getName())
    }


    @CommandBody
    val get = subCommand {
        dynamic {
            suggestion<ProxyPlayer>(uncheck = true) { sender, context ->
                RandomItem.randomItemManager.map { it.key }
            }
            execute<ProxyPlayer> { sender, context, argument ->
                if (!sender.hasPermission("ri.command.get")) {
                    sender.sendLang("command-no-permission")
                    return@execute
                }
                if (!argument.contains(" ")) {
                    val rItem = RandomItem.randomItemManager[argument]
                    if (rItem == null) {
                        sender.sendLang("command-valid-item", argument)
                        return@execute
                    }
                    val item = rItem.give(sender)
                    sender.sendLang("command-get-item", item.getName())
                } else {
                    val array = argument.split(" ")
                    val itemKey = array[0]
                    val rItem = RandomItem.randomItemManager[itemKey]
                    if (rItem == null) {
                        sender.sendLang("command-valid-item", itemKey)
                        return@execute
                    }
                    handleAndGive(rItem, argument, sender, sender)
                }
            }
        }
    }

    @CommandBody
    val delete = subCommand {
        dynamic {
            suggestion<ProxyPlayer>(uncheck = true) { sender, context ->
                RandomItem.randomItemManager.map { it.key }
            }
            execute<ProxyPlayer> { sender, context, argument ->
                if (!sender.hasPermission("ri.command.get")) {
                    sender.sendLang("command-no-permission")
                    return@execute
                }
                val rItem = RandomItem.randomItemManager[argument]
                if (rItem == null) {
                    sender.sendLang("command-valid-item", argument)
                    return@execute
                }
                rItem.unRegister()
                sender.sendLang("command-delete-item", rItem.key)
            }
        }

    }

    @CommandBody(permission = "ri.command.give")
    val give = subCommand {
        dynamic {
            suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                onlinePlayers().map { it.name }
            }
            dynamic {
                suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                    RandomItem.randomItemManager.map { it.key }
                }
                execute<ProxyCommandSender> { sender, context, argument ->
                    if (!sender.hasPermission("ri.command.give")) {
                        sender.sendLang("command-no-permission")
                        return@execute
                    }
                    val playerName = context.argument(-1)
                    val proxyPlayer = getProxyPlayer(playerName)
                    if (proxyPlayer == null) {
                        sender.sendLang("command-valid-player", playerName)
                        return@execute
                    }
                    val array = argument.split(" ")
                    val itemKey = array[0]
                    val rItem = RandomItem.randomItemManager[itemKey]
                    if (rItem == null) {
                        sender.sendLang("command-valid-item", itemKey)
                        return@execute
                    }
                    handleAndGive(rItem, argument, sender, proxyPlayer)
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            if (!sender.hasPermission("ri.command.reload")) {
                sender.sendLang("command-no-permission")
                return@execute
            }
            RandomItem.reload()
            sender.sendLang("command-reload")
        }
    }

    @CommandBody
    val drop = subCommand {
        dynamic {
            suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                onlinePlayers().map { it.name }
            }
            dynamic {
                suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                    RandomItem.randomItemManager.map { it.key }
                }
                dynamic {
                    suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                        Bukkit.getWorlds().map { it.name }
                    }
                    dynamic {
                        suggestion<ProxyPlayer>(uncheck = true) { sender, context ->
                            listOf(sender.location.x.toString())
                        }
                        restrict<ProxyCommandSender> { sender, context, argument ->
                            Coerce.asDouble(argument).isPresent
                        }
                        dynamic {
                            suggestion<ProxyPlayer>(uncheck = true) { sender, context ->
                                listOf(sender.location.y.toString())
                            }
                            restrict<ProxyCommandSender> { sender, context, argument ->
                                Coerce.asDouble(argument).isPresent
                            }
                            dynamic {
                                suggestion<ProxyPlayer>(uncheck = true) { sender, context ->
                                    listOf(sender.location.z.toString())
                                }
                                restrict<ProxyCommandSender> { sender, context, argument ->
                                    Coerce.asDouble(argument).isPresent
                                }
                                execute<ProxyCommandSender> { sender, context, argument ->
                                    if (!sender.hasPermission("ri.command.drop")) {
                                        sender.sendLang("command-no-permission")
                                        return@execute
                                    }

                                    val playerName = context.argument(-5)
                                    val proxyPlayer = getProxyPlayer(playerName)
                                    if (proxyPlayer == null) {
                                        sender.sendLang("command-valid-player", playerName)
                                        return@execute
                                    }

                                    val itemKey = context.argument(-4)
                                    val rItem = RandomItem.randomItemManager[itemKey]
                                    if (rItem == null) {
                                        sender.sendLang("command-valid-item", itemKey)
                                        return@execute
                                    }

                                    val worldKey = context.argument(-3)
                                    val world = Bukkit.getWorld(worldKey)
                                    if (world == null) {
                                        sender.sendLang("command-valid-world", worldKey)
                                        return@execute
                                    }

                                    val x = context.argument(-2).toDouble()
                                    val y = context.argument(-1).toDouble()
                                    val z: Double
                                    val pair: Pair<List<ItemStack>, Boolean>
                                    if (!argument.contains(" ")) {
                                        z = argument.toDouble()
                                        pair = handle(rItem, "", proxyPlayer.cast<Player>())
                                    } else {
                                        z = argument.split(" ")[0].toDouble()
                                        pair = handle(
                                            rItem,
                                            argument.replace(argument.split(" ")[0], ""),
                                            proxyPlayer.cast<Player>()
                                        )
                                    }
                                    val location = Location(world, x, y, z)
                                    val list = pair.first
                                    val isSame = pair.second
                                    if (list.isEmpty()) return@execute
                                    list.forEach {
                                        world.dropItem(location, it)
                                        if (!isSame)
                                            sender.sendLang(
                                                "command-drop-item",
                                                it.getName(),
                                                "1",
                                                worldKey,
                                                x.toString(),
                                                y.toString(),
                                                z.toString()
                                            )
                                    }
                                    if (isSame)
                                        sender.sendLang(
                                            "command-drop-item",
                                            list[0].getName(),
                                            "${list.size}",
                                            worldKey,
                                            x.toString(),
                                            y.toString(),
                                            z.toString()
                                        )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getConfigList(deepPath: String): String {
        return RandomItem.configManager["config"].getString("list.$deepPath").toString().colored()
    }

    @CommandBody
    val list = subCommand {
        dynamic(optional = true) {
            restrict<ProxyCommandSender> { sender, context, argument ->
                Coerce.asInteger(argument).isPresent
            }
            execute<ProxyCommandSender> { sender, context, argument ->
                submit(async = true) {
                    val rItems = RandomItem.randomItemManager.values.toList()

                    val page = argument.toIntOrNull() ?: 1

                    val size = getConfigList("pre-page-size").toIntOrNull() ?: 10
                    val total = RandomItem.randomItemManager.size
                    val lastPage = total / size + if (total % size != 0) 1 else 0

                    val up = getConfigList("up")
                    val format = getConfigList("format")
                    val leftSymbol = getConfigList("left")
                    val rightSymbol = getConfigList("right")
                    val down = getConfigList("down")

                    submit { sender.sendMessage(up) }
                    var raw: TellrawJson
                    val beginIndex = (page - 1) * size
                    val lastIndex = if (page != lastPage) {
                        size * page
                    } else {
                        total
                    } - 1
                    for (index in beginIndex..lastIndex) {
                        val rItem = rItems[index]
                        raw = TellrawJson()
                            .append(
                                format.replacement(
                                    mapOf(
                                        "{order}" to "${index + 1}",
                                        "{key}" to rItem.key,
                                        "{name}" to rItem.metaData[displayMeta].toString().colored()
                                    )
                                )
                            )
                            .hoverItem(rItem.product())
                        if (sender is ProxyPlayer) {
                            raw.runCommand("/ri get ${rItem.key}")
                        }
                        sync { raw.sendTo(sender) }
                    }
                    raw = TellrawJson()
                    val previousPage = page - 1
                    val left = TellrawJson().append(leftSymbol)
                    if (previousPage > 0) {
                        left.runCommand("/ri list $previousPage")
                    }

                    val nextPage = page - 1
                    val right = TellrawJson().append(rightSymbol)
                    if (nextPage > 0) {
                        right.runCommand("/ri list $nextPage")
                    }

                    val pageInfo =
                        getConfigList("page-info").replacement(mapOf("{current}" to "$page", "{total}" to "$lastPage"))

                    submit {
                        raw.append(left)
                            .append(pageInfo)
                            .append(right)
                            .sendTo(sender)
                        sender.sendMessage(down)
                    }
                }
            }
        }
        execute<ProxyCommandSender> { sender, context, argument ->
            sender.performCommand("ri list 1")
        }
    }

    @CommandBody
    val save = subCommand {
        dynamic {
            dynamic {
                restrict<Player> { sender, context, argument ->
                    argument.endsWith(".yml")
                }
                execute<Player> { sender, context, argument ->
                    if (!sender.hasPermission("ri.command.save")) {
                        sender.sendLang("command-no-permission")
                        return@execute
                    }
                    val itemInHand = sender.inventory.itemInMainHand
                    if (itemInHand.isAir()) {
                        sender.sendLang("command-valid-item-in-hand")
                        return@execute
                    }
                    val key = context.argument(-1)
                    if (RandomItem.randomItemManager.containsKey(key)) {
                        sender.sendLang("command-id-exists", key)
                        return@execute
                    }
                    val rItem = RItem.createFromItem(key, itemInHand)
                    submit(async = true) {
                        val file = newFile(RandomItem.plugin.dataFolder, "items/$argument")
                        val config = YamlConfiguration.loadConfiguration(file)
                        config[key] = rItem.serialize()
                        config.save(file)
                        RandomItem.reload()
                    }
                    sender.sendLang("command-save-item", key, argument)
                }
            }
        }
    }

    @CommandBody
    val refresh = subCommand {
        dynamic {
            execute<Player> { sender, context, argument ->
                if (!sender.hasPermission("ri.command.refresh")) {
                    sender.sendLang("command-no-permission")
                    return@execute
                }
                val itemInHand = sender.inventory.itemInMainHand
                if (itemInHand.isAir()) {
                    sender.sendLang("command-valid-item-in-hand")
                    return@execute
                }
                val itemTag = itemInHand.getItemTag()
                if (!itemTag.containsKey("VARIABLES_VALUE") || !itemTag.containsKey("RANDOM_ITEM")) {
                    sender.sendLang("command-valid-random-item-in-hand")
                    return@execute
                }

                val demand = Demand("ad $argument")
                val refreshes = demand.get("vars", "all")!!.toArgs()
                val data = demand.get("data", "[]")!!
                val processData = ProcessData.pointData(sender, data)
                val compound = itemTag["VARIABLES_VALUE"]!!.asCompound()
                for (key in compound.keys) {
                    if (processData.containsKey(key)) continue
                    val value = (compound[key] ?: continue).obj()
                    if (value == "NULL") continue
                    if (refreshes.contains("all") || refreshes.contains(key)) continue
                    processData[key] = value
                }

                val rItemKey = itemTag["RANDOM_ITEM"]!!.asString()
                if (!RandomItem.randomItemManager.containsKey(rItemKey)) {
                    sender.sendLang("command-valid-item", rItemKey)
                    return@execute
                }

                val rItemOld = RandomItem.randomItemManager[rItemKey]!!
                val newItem = rItemOld.product(sender, processData)

                sender.inventory.setItemInMainHand(newItem)
                sender.sendLang(
                    "command-refresh-item",
                    itemInHand.getName(),
                    newItem.getName(),
                    refreshes.toList().toString(),
                    data
                )
            }
        }
    }

    @JvmStatic
    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun ItemTagData.obj(): Any {
        val value = when (this.type) {
            ItemTagType.BYTE -> this.asByte()
            ItemTagType.SHORT -> this.asShort()
            ItemTagType.INT -> this.asInt()
            ItemTagType.LONG -> this.asLong()
            ItemTagType.FLOAT -> this.asFloat()
            ItemTagType.DOUBLE -> this.asDouble()
            ItemTagType.STRING -> this.asString()
            ItemTagType.BYTE_ARRAY -> this.asByteArray()
            ItemTagType.INT_ARRAY -> this.asIntArray()
            ItemTagType.COMPOUND -> this.asCompound()
            ItemTagType.LIST -> this.asList()
            else -> this.asString()
        }
        return when (value) {
            is ItemTag -> {
                value.toMutableMap()
            }
            is ItemTagList -> {
                val list = java.util.ArrayList<Any>()
                value.forEach {
                    list.add(it.obj())
                }
                list
            }
            else -> {
                value
            }
        }
    }
}