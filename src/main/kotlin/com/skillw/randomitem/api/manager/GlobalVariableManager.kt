package com.skillw.randomitem.api.manager

import com.skillw.pouvoir.api.manager.Manager
import com.skillw.pouvoir.api.map.KeyMap
import com.skillw.randomitem.api.variable.Variable

/**
 * 全局变量管理器
 */
abstract class GlobalVariableManager : Manager, KeyMap<String, Variable>()