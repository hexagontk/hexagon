package com.hexagonkt

import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.runBlocking

/** Syntax sugar to throw errors. */
val error: Nothing get() = error("Invalid state")

// MAP OPERATIONS //////////////////////////////////////////////////////////////////////////////////
/**
 * TODO .
 */
@Suppress("UNCHECKED_CAST")
operator fun Map<*, *>.get(vararg keys: Any): Any? =
    if (keys.size > 1)
        keys
            .dropLast(1)
            .fold(this) { result, element ->
                val r = result as Map<Any, Any>
                val value = r.getOrElse(element) { mapOf<Any, Any>() }
                when (value) {
                    is Map<*, *> -> value
                    is List<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
                    else -> mapOf<Any, Any>()
                }
            }[keys.last()]
    else
        (this as Map<Any, Any>).getOrElse(keys.first()) { null }

@Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
fun <T : Any> Map<*, *>.require(vararg name: Any): T =
    this.get(*name) as? T ?: error("$name required setting not found")

fun <K, V> Map<K, V>.filterEmpty(): Map<K, V> = this.filterValues(::notEmpty)

fun <V> List<V>.filterEmpty(): List<V> = this.filter(::notEmpty)

fun <V> notEmpty(it: V): Boolean {
    return when (it) {
        null -> false
        is List<*> -> it.isNotEmpty()
        is Map<*, *> -> it.isNotEmpty()
        else -> true
    }
}

// KOTLIN //////////////////////////////////////////////////////////////////////////////////////////
fun sync(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {

    runBlocking(context, block)
}
