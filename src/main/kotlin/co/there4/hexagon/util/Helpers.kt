package co.there4.hexagon.util

import java.io.InputStream
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getProperty
import java.lang.Thread.currentThread
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.net.InetAddress.getLocalHost
import java.net.URL
import java.time.*
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*

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
    (this.toLocalDate().asNumber() * 1_000_000_000L) +
    this.toLocalTime().asNumber()

fun LocalDate.asNumber(): Int =
    (this.year       * 10_000) +
    (this.monthValue * 100) +
    this.dayOfMonth

fun LocalTime.asNumber(): Int =
    (this.hour       * 10_000_000) +
    (this.minute     * 100_000) +
    (this.second     * 1_000) +
    (this.nano / 1_000_000) // Nanos to millis

fun LocalDateTime.formatToIso(): String = this.format(ISO_DATE_TIME)
fun LocalDateTime.withZone(zoneId: ZoneId = timeZone.toZoneId()): ZonedDateTime =
    ZonedDateTime.of(this, zoneId)

/**
 * Parses a date from a formatted integer with this format: `YYYYMMDDHHmmss`.
 */
fun Long.toLocalDateTime(): LocalDateTime = (this / 1_000_000_000).toInt()
    .toLocalDate()
    .atTime((this % 1_000_000_000).toInt().toLocalTime())

fun Int.toLocalDate(): LocalDate = LocalDate.of(
    this / 10_000,
    (this % 10_000) / 100,
    this % 100
)

fun Int.toLocalTime(): LocalTime = LocalTime.of(
    (this / 10_000_000),
    ((this % 10_000_000) / 100_000),
    ((this % 100_000) / 1_000),
    ((this % 1_000) * 1_000_000) // Millis to nanos
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

/**
 * Returns the code location in the caller of this function.
 *
 * @param offset Steps up in the stack. Ie: to get the caller of the caller.
 */
fun caller(offset: Int = 0): String = currentThread ().stackTrace.let {
    val frame = it[3 + (if (offset > 0) offset - 1 else offset)] // Because of default parameter
    "${frame.className} ${frame.methodName} ${frame.fileName} ${frame.lineNumber}"
}

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

/*
 * Networking
 */

/** Unknown host name. */
const val UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST"

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
        this.filterStackTrace(prefix).map { "\tat $it" }.joinToString(EOL, EOL) +
        if (this.cause == null)
            ""
        else
            "${EOL}Caused by: " + (this.cause as Throwable).toText (prefix)

val err: Nothing get() = error("Invalid state")

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

fun <T : Any> flistOf(vararg pairs: T?): List<T> = listOf(*pairs)
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
val systemClassLoader: ClassLoader = getSystemClassLoader()

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
object Log : CachedLogger(Log::class)

internal val flarePrefix = getProperty ("CachedLogger.flarePrefix", ">>>>>>>>")
val jvmId: String = getRuntimeMXBean().name
