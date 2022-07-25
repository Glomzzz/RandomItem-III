package com.skillw.randomitem.util

import com.skillw.randomitem.RandomItem
import taboolib.common5.Mirror
import taboolib.common5.mirrorFuture
import taboolib.common5.mirrorNow
import java.util.concurrent.CompletableFuture

/**
 * @className Mirror
 * @author Glom
 * @date 2022/7/15 10:03
 * Copyright  2022 user. All rights reserved.
 */
fun <T> mirrorFutureA(id: String, func: Mirror.MirrorFuture<T>.() -> Unit): CompletableFuture<T> {
    return RandomItem.poolExecutor.submit<CompletableFuture<T>> { mirrorFuture(id, func) }.get()
}

fun <T> mirrorNowA(id: String, func: () -> T): T {
    return RandomItem.poolExecutor.submit<T> { mirrorNow(id, func) }.get()
}