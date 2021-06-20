package com.hexagonkt.injection

import kotlin.reflect.KClass

data class Target<T : Any>(
    val type: KClass<T>,
    val tag: Any = Unit,
) {
    companion object {
        inline fun <reified T: Any> Target(tag: Any = Unit) =
            Target(T::class, tag)
    }

    override fun toString(): String =
        if (tag == Unit) type.toString()
        else "$type with tag '$tag'"
}
