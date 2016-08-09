package co.there4.hexagon.util

import java.io.InputStream
import java.lang.System.*
import java.lang.ThreadLocal.withInitial
import java.net.InetAddress.getLocalHost
import java.time.LocalDateTime
import java.util.*
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.lang.ClassLoader.getSystemClassLoader

/*
 * Timing
 * TODO Use Kotlin's kotlin.system.measureTimeMillis and remove these
 */

private val times = withInitial { LinkedList<Long> () }

/**
 * Store a timestamp in nanoseconds in the thread's times stack.
 */
fun resetTimes() = times.get ().clear()
/**
 * Store a timestamp in nanoseconds in the thread's times stack.
 */
fun pushTime() = times.get ().push(nanoTime())
/**
 * Pop latest timestamp in the nanos times stack and returns the difference with current one.
 */
fun popTime() = nanoTime() - times.get().pop()
/**
 * Returns a time difference in nanoseconds formatted as a string.
 */
fun formatTime(timestamp: Long) = "%1.3f ms".format (timestamp / 1e6)

/**
 * Formats a date as a formatted integer with this format: `YYYYMMDDHHmmss`.
 */
fun LocalDateTime.asInt () =
    (this.year       * 1e10.toLong()) +
    (this.monthValue * 1e8.toLong()) +
    (this.dayOfMonth * 1e6.toLong()) +
    (this.hour       * 1e4.toLong()) +
    (this.minute     * 1e2.toLong()) +
    this.second

fun LocalDateTime.toDate(): Date = Date.from(this.toInstant(UTC))
fun LocalDate.toDate(): Date = this.atStartOfDay().toDate()

fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this.time), UTC)

fun Date.toLocalDate(): LocalDate = this.toLocalDateTime().toLocalDate()

/*
 * Threading
 */

/** Map for storing context data linked to the executing thread. */
object Context {
    private val threadLocal = withInitial { LinkedHashMap<Any, Any>() }
    fun entries () = threadLocal.get().entries
    operator fun get (key: Any) = threadLocal.get()[key]
    operator fun set (key: Any, value: Any) { threadLocal.get()[key] = value }
}

/**
 * Executes a lambda until no exception is thrown or a number of times is reached.
 *
 * @param times Number of times to try to execute the callback. Must be greater than 0.
 * @param delay Milliseconds to wait to next execution if there was an error. Must be 0 or greater.
 * @return The callback result if succeed.
 * @throws [ServiceException] if the callback didn't succeed in the given times.
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

    throw ServiceException(0, "Error retrying $times times ($delay ms)", *exceptions.toTypedArray())
}

/*
 * Networking
 */

/** Unknown host name. */
val UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST"

/** The hostname of the machine running this program. */
val hostname = getLocalHost()?.getHostName() ?: UNKNOWN_LOCALHOST
/** The IP address of the machine running this program. */
val ip = getLocalHost()?.getHostAddress() ?: UNKNOWN_LOCALHOST

fun parseQueryParameters(query: String): Map<String, String> =
    if (query.isEmpty())
        mapOf()
    else
        query.split("&".toRegex())
            .map {
                val kv = it.split("=")
                kv[0].trim () to (if (kv.size == 2) kv[1].trim() else "")
            }
            .toMap(LinkedHashMap<String, String>())

/*
 * Error handling
 */

/**
 * Returns the stack trace array of the frames that starts with the given prefix.
 */
fun Throwable.filterStackTrace (prefix: String) =
    if (prefix.isEmpty ())
        this.stackTrace
    else
        this.stackTrace.filter { it.className.startsWith (prefix) }.toTypedArray()

/**
 * Returns this throwable as a text.
 */
fun Throwable.toText (prefix: String = ""): String =
    "${this.javaClass.name}: ${this.message}" +
        this.filterStackTrace(prefix).map { "\tat ${it.toString()}" }.joinToString(EOL, EOL) +
        if (this.cause == null)
            ""
        else
            "${EOL}Caused by: " + (this.cause as Throwable).toText (prefix)

/*
 * Logging
 */

internal val flarePrefix = getProperty ("CompanionLogger.flarePrefix", ">>>>>>>>")
val jvmId = getRuntimeMXBean().name

/*
 * Map operations
 */

@Suppress("UNCHECKED_CAST")
operator fun Map<*, *>.get(vararg keys: Any): Any? =
    if (keys.size > 1)
        keys
            .dropLast(1)
            .fold(this) { result, element ->
                val r = result as Map<Any, Any>
                val value = r.getOrElse(element, { mapOf<Any, Any>() })
                when (value) {
                    is Map<*, *> -> value
                    is List<*> -> value.mapIndexed { ii, item -> ii to item  }.toMap()
                    else -> mapOf<Any, Any>()
                }
            }[keys.last()]
    else
        (this as Map<Any, Any>).getOrElse(keys.first()) { null }

/*
 * I/O
 */
val systemClassLoader: ClassLoader = getSystemClassLoader() ?: error("Error getting class loader")

fun resourceAsStream(resName: String): InputStream? = systemClassLoader.getResourceAsStream(resName)
