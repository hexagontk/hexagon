package com.hexagonkt.core.args

import kotlin.reflect.KClass

// TODO Use 'group' to group similar options:
//   val group: String? = null,
data class Option<T : Any>(
    val shortName: Char,
    val longName: String? = null,
    val type: KClass<T>,
    val description: String? = null,
    val optional: Boolean = true,
    val defaultValue: T? = null,
)
