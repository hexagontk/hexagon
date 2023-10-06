package com.hexagonkt.handlers

import kotlin.reflect.KClass
import kotlin.reflect.cast

internal fun <T : Exception> castException(exception: Exception?, exceptionClass: KClass<T>): T =
    exception
        ?.let { exceptionClass.cast(exception) }
        ?: error("Exception 'null' or incorrect type")
