package com.hexagonkt

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.lang.System.nanoTime
import kotlin.reflect.KClass

/** Default logger when you are lazy to declare one. */
val logger: Logger = loggerOf(Environment::class)

internal const val FLARE_PREFIX = ">>>>>>>>"

fun loggerOf(type: KClass<*>): Logger = getLogger(type.java)

fun Any.logger(): Logger = loggerOf(this::class)

fun Logger.trace (message: Any?) { if (this.isTraceEnabled) this.trace(message.toString()) }

fun Logger.debug (message: Any?) { if (this.isDebugEnabled) this.debug(message.toString()) }

fun Logger.info (message: Any?) { if (this.isInfoEnabled) this.info(message.toString()) }

fun Logger.warn (message: Any?) { if (this.isWarnEnabled) this.warn(message.toString()) }

fun Logger.error(message: Any?) { if (this.isErrorEnabled) this.error(message.toString()) }

fun Logger.warn (message: Any?, exception: Throwable) {
    if (this.isWarnEnabled) this.warn(message.toString(), exception)
}

fun Logger.error(message: Any?, exception: Throwable) {
    if (this.isErrorEnabled) this.error(message.toString(), exception)
}

fun Logger.flare (message: Any? = "") {
    if (this.isTraceEnabled) trace("$FLARE_PREFIX $message")
}

fun Logger.time (startNanos: Long, message: Any?) {
    if (this.isTraceEnabled)
        this.trace("${message ?: "TIME"} : ${formatNanos(nanoTime() - startNanos)}")
}

fun <T> Logger.time (message: Any? = null, block: () -> T): T {
    val start = nanoTime()
    return block().also { time (start, message) }
}
