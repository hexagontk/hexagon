package com.hexagonkt.core.args

import kotlin.reflect.KClass

data class Option<T : Any>(
    val shortName: Char,
    val longName: String? = null,
    val type: KClass<T>,
    val description: String? = null,
    val optional: Boolean = true,
    val defaultValue: T? = null,
)
