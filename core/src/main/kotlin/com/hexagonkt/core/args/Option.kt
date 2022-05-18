package com.hexagonkt.core.args

import kotlin.reflect.KClass
import kotlin.reflect.cast

data class Option<T : Any>(
    val shortName: Char,
    val longName: String? = null,
    val type: KClass<T>,
    val description: String? = null,
    val optional: Boolean = true,
    val defaultValue: T? = null,
    //val typeCreator: (String) -> T, // this would be needed if we want to support non-primitive types
)

class Options(private val values: Map<Option<*>, Any>) {
    fun <T : Any> get(option: Option<T>): T? {
        val value = values[option] ?: return null
        return option.type.cast(value)
    }
}
