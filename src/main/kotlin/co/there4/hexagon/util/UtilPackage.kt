package co.there4.hexagon.util

import java.io.InputStream
import java.lang.System.*
import java.lang.ThreadLocal.withInitial
import java.net.InetAddress.getLocalHost
import java.util.*
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URL
import java.time.*
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

/*
 * Timing
 */

val timeZone: TimeZone = TimeZone.getDefault()

/**
 * Returns a time difference in nanoseconds formatted as a string.
 */
fun formatNanos(timestamp: Long) = "%1.3f ms".format (timestamp / 1e6)

/**
 * Formats a date as a formatted integer with this format: `YYYYMMDDHHmmss`.
 */
fun LocalDateTime.asNumber(): Long =
    (this.toLocalDate().asNumber() * 1e9.toLong()) +
    this.toLocalTime().asNumber()

fun LocalDate.asNumber(): Int =
    (this.year       * 1e4.toInt()) +
    (this.monthValue * 1e2.toInt()) +
    this.dayOfMonth

fun LocalTime.asNumber(): Int =
    (this.hour       * 1e7.toInt()) +
    (this.minute     * 1e5.toInt()) +
    (this.second     * 1e3.toInt()) +
    (this.nano / 1e6.toInt()) // Nanos to millis

fun LocalDateTime.formatToIso(): String = this.format(ISO_DATE_TIME)
fun LocalDateTime.withZone(zoneId: ZoneId = timeZone.toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

/**
 * Parses a date from a formatted integer with this format: `YYYYMMDDHHmmss`.
 */
fun Long.toLocalDateTime(): LocalDateTime = (this / 1e9).toInt()
    .toLocalDate()
    .atTime((this % 1e9.toLong()).toInt().toLocalTime())

fun Int.toLocalDate(): LocalDate = LocalDate.of(
    this / 1e4.toInt(),
    (this % 1e4.toInt()) / 1e2.toInt(),
    this % 1e2.toInt()
)

fun Int.toLocalTime(): LocalTime = LocalTime.of(
    (this / 1e7.toInt()),
    ((this % 1e7.toInt()) / 1e5.toInt()),
    ((this % 1e5.toInt()) / 1e3.toInt()),
    ((this % 1e3.toInt()) * 1e6.toInt()) // Millis to nanos
)

fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
fun LocalDateTime.toDate(): Date = this.atZone(timeZone.toZoneId()).toDate()
fun LocalDate.toDate(): Date = this.atStartOfDay(timeZone.toZoneId()).toDate()

fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this.time), ZoneId.systemDefault())

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
val hostname = getLocalHost()?.hostName ?: UNKNOWN_LOCALHOST
/** The IP address of the machine running this program. */
val ip = getLocalHost()?.hostAddress ?: UNKNOWN_LOCALHOST

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
        this.filterStackTrace(prefix).map { "\tat $it" }.joinToString(EOL, EOL) +
        if (this.cause == null)
            ""
        else
            "${EOL}Caused by: " + (this.cause as Throwable).toText (prefix)

fun error(): Nothing = error("Invalid state")
val err: Nothing get() = error()

/*
 * Logging
 */

internal val flarePrefix = getProperty ("CompanionLogger.flarePrefix", ">>>>>>>>")
val jvmId: String = getRuntimeMXBean().name

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

fun <K : Any, V : Any> fmapOf(vararg pairs: Pair<K, V?>): Map<K, V> = mapOf(*pairs)
    .filterValues {
        when (it) {
            null -> false
            is List<*> -> it.isNotEmpty()
            is Map<*, *> -> it.isNotEmpty()
            else -> true
        }
    }
    .mapValues { it.value ?: err }

fun <T : Any> flistOf(vararg pairs: T?): List<T> = listOf<T?>(*pairs)
    .filter {
        when (it) {
            null -> false
            is List<*> -> it.isNotEmpty()
            is Map<*, *> -> it.isNotEmpty()
            else -> true
        }
    }
    .map { it ?: err }

/*
 * I/O
 */
val systemClassLoader: ClassLoader = getSystemClassLoader() ?: error("Error getting class loader")

/**
 * TODO Fix class loader issues, use thread class loader or whatever
 * http://www.javaworld.com/article/2077344/core-java/find-a-way-out-of-the-classloader-maze.html
 */
fun resourceAsStream(resName: String): InputStream? = systemClassLoader.getResourceAsStream(resName)
fun resource(resName: String): URL? = systemClassLoader.getResource(resName)
fun requireResource(resName: String): URL = resource(resName) ?: error("$resName not found")
//fun resources(resName: String): List<URL> =
//    systemClassLoader.getResources(resName).toList().filterNotNull()

/*
 * Logging
 */
object Log : CompanionLogger(Log::class)
