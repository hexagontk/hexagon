package com.hexagonkt.helpers

import java.net.ServerSocket
import java.net.Socket
import com.hexagonkt.logging.Logger

/** Default logger for when you feel too lazy to declare one. */
val logger: Logger = Logger(Logger::class)

// NETWORK /////////////////////////////////////////////////////////////////////////////////////////
/**
 * Return a random free port (not used by any other local process).
 *
 * @return Random free port number.
 */
fun freePort(): Int =
    ServerSocket(0).use { it.localPort }

/**
 * Check if a port is already opened.
 *
 * @param port Port number to check.
 * @return True if the port is open, false otherwise.
 */
fun isPortOpened(port: Int): Boolean =
    try {
        Socket("localhost", port).use { it.isConnected }
    }
    catch (e: Exception) {
        false
    }

// THREADING ///////////////////////////////////////////////////////////////////////////////////////
/**
 * Execute a lambda until no exception is thrown or a number of times is reached.
 *
 * @param times Number of times to try to execute the callback. Must be greater than 0.
 * @param delay Milliseconds to wait to next execution if there was an error. Must be 0 or greater.
 * @param block Code to be executed.
 * @return Callback's result if succeed.
 * @throws [MultipleException] if the callback didn't succeed in the given times.
 */
fun <T> retry(times: Int, delay: Long, block: () -> T): T {
    require(times > 0)
    require(delay >= 0)

    val exceptions = mutableListOf<Exception>()
    for (ii in 1 .. times) {
        try {
            return block()
        }
        catch (e: Exception) {
            exceptions.add(e)
            Thread.sleep(delay)
        }
    }

    throw MultipleException("Error retrying $times times ($delay ms)", exceptions)
}

// ERROR HANDLING //////////////////////////////////////////////////////////////////////////////////
/** Syntax sugar to throw errors. */
val fail: Nothing
    get() = error("Invalid state")

/**
 * Return the stack trace array of the frames that starts with the given prefix.
 *
 * @receiver Throwable which stack trace will be filtered.
 * @param prefix Prefix used to filter stack trace elements (applied to class names).
 * @return Array with the frames of the throwable whose classes start with the given prefix.
 */
fun Throwable.filterStackTrace(prefix: String): Array<out StackTraceElement> =
    if (prefix.isEmpty())
        this.stackTrace
    else
        this.stackTrace.filter { it.className.startsWith(prefix) }.toTypedArray()

/**
 * Return this throwable as a text.
 *
 * @receiver Throwable to be printed to a string.
 * @param prefix Optional prefix to filter stack trace elements.
 * @return The filtered (if filter is provided) Throwable as a string.
 */
fun Throwable.toText(prefix: String = ""): String =
    "${this.javaClass.name}: ${this.message}" +
        this.filterStackTrace(prefix).joinToString(eol, eol) { "\tat $it" } +
        if (this.cause == null)
            ""
        else
            "${eol}Caused by: " + (this.cause as Throwable).toText(prefix)

// COLLECTIONS /////////////////////////////////////////////////////////////////////////////////////
/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param count .
 * @return .
 */
fun <Z> Collection<Z>.ensureSize(count: IntRange): Collection<Z> = this.apply {
    if (size !in count) error("$size items while expecting only $count element")
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param keys .
 * @return .
 */
@Suppress("UNCHECKED_CAST")
operator fun Map<*, *>.get(vararg keys: Any): Any? =
    if (keys.size > 1)
        keys
            .dropLast(1)
            .fold(this) { result, element ->
                val r = result as Map<Any, Any>
                when (val value = r[element]) {
                    is Map<*, *> -> value
                    is List<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
                    else -> emptyMap<Any, Any>()
                }
            }[keys.last()]
    else
        (this as Map<Any, Any>).getOrElse(keys.first()) { null }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
@Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
fun <T : Any> Map<*, *>.requireKeys(vararg name: Any): T =
    this.get(*name) as? T ?: error("$name required key not found")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
fun <K, V> Map<K, V>.require(name: K): V =
    this[name] ?: error("$name required key not found")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <K, V> Map<K, V?>.filterEmpty(): Map<K, V> =
    this.filterValues(::notEmpty).mapValues { (_, v) -> v ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @return .
 */
fun <V> List<V?>.filterEmpty(): List<V> =
    this.filter(::notEmpty).map { it ?: fail }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param value .
 * @return .
 */
fun <V> notEmpty(value: V?): Boolean {
    return when (value) {
        null -> false
        is List<*> -> value.isNotEmpty()
        is Map<*, *> -> value.isNotEmpty()
        else -> true
    }
}
