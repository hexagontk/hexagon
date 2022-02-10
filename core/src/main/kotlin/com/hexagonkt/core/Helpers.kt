package com.hexagonkt.core

import com.hexagonkt.core.logging.logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 *  Disable heavy and optional checks in runtime. This flag can be enabled to get a small
 *  performance boost. Do *NOT* do this on development (it could mask problems) and enable it on
 *  production only if you have tested your application extensively.
 *
 *  It's initial value is taken from the `DISABLE_CHECKS` flag. See [Jvm.systemFlag] for details on
 *  how flags are checked on a JVM.
 *
 *  This variable can be changed on code to affect only certain parts of the code, however this is
 *  not advised and should be done carefully.
 */
var disableChecks: Boolean = Jvm.systemFlag("DISABLE_CHECKS")

/**
 * Print receiver to stdout. Convenient utility to debug variables quickly.
 *
 * @receiver Reference to the object to print. Can be `null`.
 * @param prefix String to print before the actual object information. Empty string by default.
 * @return Receiver's reference. Returned to allow method call chaining.
 */
fun <T> T.println(prefix: String = ""): T =
    apply { kotlin.io.println("$prefix$this") }

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param locale .
 * @return .
 */
inline fun <reified T : ResourceBundle> resourceBundle(
    locale: Locale = Locale.getDefault()): ResourceBundle =
        resourceBundle(T::class, locale)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param type .
 * @param locale .
 * @return .
 */
fun <T : ResourceBundle> resourceBundle(
    type: KClass<T>, locale: Locale = Locale.getDefault()): ResourceBundle =
        ResourceBundle.getBundle(type.java.name, locale)

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
        logger.debug { "Checked port: $port is already open" }
        false
    }

// PROCESSES ///////////////////////////////////////////////////////////////////////////////////////
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

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * TODO Assure JVM closes properly after process execution (dispose process resources, etc.)
 */
fun List<String>.exec(
    workingDirectory: File = File(System.getProperty("user.dir")),
    timeout: Long = Long.MAX_VALUE,
    fail: Boolean = false,
): String {

    val command = filter { it.isNotBlank() }.toTypedArray()

    require(command.isNotEmpty()) { "Command is empty" }
    require(timeout > 0) { "Process timeout should be greater than zero: $timeout" }

    val process = ProcessBuilder(*command).directory(workingDirectory).start()

    if (!process.waitFor(timeout, SECONDS)) {
        process.destroy()
        error("Command timed out: $this")
    }

    val exitValue = process.exitValue()
    val output = BufferedReader(InputStreamReader(process.inputStream)).readText()

    if (fail && exitValue != 0)
        throw CodedException(exitValue, output)

    return output
}

/**
 * TODO Add use case and example in documentation.
 * TODO Support multiple words parameters by processing " and '
 *
 * Run the receiver's text as a process in the host operating system. The command can have multiple
 * lines and may or may not contain the shell continuation string (` \\n`).
 *
 * @receiver String holding the command to be executed.
 * @param workingDirectory Directory on which the process will be executed. Defaults to current
 *  directory.
 * @param timeout Maximum number of seconds allowed for process execution. Defaults to the maximum
 *  long value. It must be greater than zero.
 * @param fail If true Raise an exception if the result code is different than zero. The default
 *  value is `false`.
 * @throws CodedException Thrown if the process return an error code (the actual code is passed
 *  inside [CodedException.code] and the command output is set at [CodedException.message].
 * @throws IllegalStateException If the command doesn't end within the allowed time or the command
 *  string is blank, an exception will be thrown.
 * @return The output of the command.
 */
fun String.exec(
    workingDirectory: File = File(System.getProperty("user.dir")),
    timeout: Long = Long.MAX_VALUE,
    fail: Boolean = false,
): String =
    replace("""(\s+\\\s*)?\n""".toRegex(), "")
        .split(" ")
        .map { it.trim() }
        .toList()
        .exec(workingDirectory, timeout, fail)

// ERROR HANDLING //////////////////////////////////////////////////////////////////////////////////
/** Syntax sugar to throw errors. */
val fail: Nothing
    get() = error("Invalid state")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 */
fun check(message: String = "Multiple exceptions", vararg blocks: () -> Unit) {
    val exceptions: List<Exception> = blocks.mapNotNull {
        try {
            it()
            null
        }
        catch(e: Exception) {
            e
        }
    }

    if (exceptions.isNotEmpty())
        throw MultipleException(message, exceptions)
}

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
fun <T : Any> Map<*, *>.keys(vararg keys: Any): T? {

    val mappedKeys = keys.map {
        when (it) {
            is KProperty1<*, *> -> it.name
            else -> it
        }
    }

    return mappedKeys
        .dropLast(1)
        .fold(this) { result, element ->
            val r = result as Map<Any, Any>
            when (val value = r[element]) {
                is Map<*, *> -> value
                is List<*> -> value.mapIndexed { ii, item -> ii to item }.toMap()
                else -> emptyMap<Any, Any>()
            }
        }[mappedKeys.last()] as? T
}

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param keys .
 * @return .
 */
operator fun <T : Any> Map<*, *>.invoke(vararg keys: Any): T? =
    keys(*keys)

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @receiver .
 * @param name .
 * @return .
 */
fun <T : Any> Map<*, *>.requireKeys(vararg name: Any): T =
    this.keys(*name) ?: error("$name required key not found")

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @param fields .
 * @return .
 */
fun <T : Any> fieldsMapOf(vararg fields: Pair<KProperty1<T, *>, *>): Map<String, *> =
    fields.associate { it.first.name to it.second }

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
