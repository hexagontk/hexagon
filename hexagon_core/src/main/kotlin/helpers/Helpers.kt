package com.hexagonkt.helpers

import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.runBlocking

import java.io.InputStream
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URL
import java.util.*

/** Default timezone. */
val timeZone: TimeZone = TimeZone.getDefault()

/** Syntax sugar to throw errors. */
val error: Nothing get() = error("Invalid state")

/** System class loader. */
val systemClassLoader: ClassLoader = getSystemClassLoader()

fun systemSetting (name: String): String? = System.getProperty(name) ?: System.getenv(name)

fun systemSetting (name: String, defaultValue: String): String = systemSetting(name) ?: defaultValue

// THREADING ///////////////////////////////////////////////////////////////////////////////////////
/**
 * Executes a lambda until no exception is thrown or a number of times is reached.
 *
 * @param times Number of times to try to execute the callback. Must be greater than 0.
 * @param delay Milliseconds to wait to next execution if there was an error. Must be 0 or greater.
 * @return The callback result if succeed.
 * @throws [CodedException] if the callback didn't succeed in the given times.
 */
fun <T> retry (times: Int, delay: Long, func: () -> T): T {
    require (times > 0)
    require (delay >= 0)

    val exceptions = mutableListOf<Exception>()
    for (ii in 1 .. times) {
        try {
            return func ()
        }
        catch (e: Exception) {
            exceptions.add (e)
            Thread.sleep (delay)
        }
    }

    throw CodedException(0, "Error retrying $times times ($delay ms)", *exceptions.toTypedArray())
}

// NETWORKING //////////////////////////////////////////////////////////////////////////////////////
/**
 * TODO .
 */
fun parseQueryParameters (query: String): Map<String, String> =
    if (query.isBlank())
        mapOf()
    else
        query.split("&".toRegex())
            .map {
                val kv = it.split("=")
                kv[0].trim () to (if (kv.size == 2) kv[1].trim() else "")
            }
            .toMap(LinkedHashMap())

// ERROR HANDLING //////////////////////////////////////////////////////////////////////////////////
/**
 * Returns the stack trace array of the frames that starts with the given prefix.
 */
fun Throwable.filterStackTrace (prefix: String): Array<out StackTraceElement> =
    if (prefix.isEmpty ())
        this.stackTrace
    else
        this.stackTrace.filter { it.className.startsWith (prefix) }.toTypedArray()

/**
 * Returns this throwable as a text.
 */
fun Throwable.toText (prefix: String = ""): String =
    "${this.javaClass.name}: ${this.message}" +
        this.filterStackTrace(prefix).joinToString(eol, eol) { "\tat $it" } +
        if (this.cause == null)
            ""
        else
            "${eol}Caused by: " + (this.cause as Throwable).toText (prefix)

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

// I/O /////////////////////////////////////////////////////////////////////////////////////////////
/**
 * TODO Fix class loader issues, use thread class loader or whatever
 * http://www.javaworld.com/article/2077344/core-java/find-a-way-out-of-the-classloader-maze.html
 */
fun resourceAsStream(resource: String): InputStream? =
    systemClassLoader.getResourceAsStream(resource)

fun resource(resource: String): URL? = systemClassLoader.getResource(resource)

fun requireResource(resource: String): URL = resource(resource) ?: error("$resource not found")

fun readResource(resource: String): String? = resourceAsStream(resource)?.reader()?.readText()

// KOTLIN //////////////////////////////////////////////////////////////////////////////////////////
fun sync(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {

    runBlocking(context, block)
}
