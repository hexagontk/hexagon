package com.hexagontk.args

import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

fun <T : Throwable> assertFailsWithMessage(
    type: KClass<T>, message: String? = null, block: () -> Unit
): T {
    val e = assertFailsWith(type) {
        block()
    }

    if (message != null)
        assertEquals(message, e.message)

    return e
}

fun assertIllegalArgument(message: String? = null, block: () -> Unit) {
    assertFailsWithMessage(IllegalArgumentException::class, message, block)
}

fun assertIllegalState(message: String? = null, block: () -> Unit) {
    assertFailsWithMessage(IllegalStateException::class, message, block)
}
