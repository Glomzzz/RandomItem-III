package com.skillw.randomitem.internal.annotation

//import com.skillw.attsystem.AttributeSystem.lineConditionManager
//import com.skillw.attsystem.api.condition.ScriptLineCondition
//import com.skillw.attsystem.internal.manager.ASConfig
//import com.skillw.pouvoir.api.manager.Manager.Companion.addSingle
//import com.skillw.pouvoir.api.script.annotation.ScriptAnnotation
//import com.skillw.pouvoir.util.MessageUtils.wrong
//
///**
// * LineCondition
// *
// * @constructor LineCondition Key Names...
// */
//object LineCondition : ScriptAnnotation("LineCondition", handle@{ data ->
//    val compiledFile = data.compiledFile
//    val args = if (data.args.contains(",")) {
//        data.args.split(",").toMutableList()
//    } else {
//        null
//    }
//    val function = data.function
//    if (args == null || args.size < 2) {
//        wrong("The ScriptAnnotation LineCondition on the function $function in ${compiledFile.key} has no enough arguments!")
//        return@handle
//    }
//    val key = "annotation-${args[0]}"
//    args.removeAt(0)
//    val names = HashSet<String>(args)
//    ScriptLineCondition(key, names, "${compiledFile.key}::$function").register()
//
//}) {
//    init {
//        ASConfig.addSingle("BeforeReload") {
//            for (it in lineConditionManager.filterKeys {
//                it.contains("annotation")
//            }) {
//                lineConditionManager.remove(it.key)
//            }
//        }
//    }
//}