package co.there4.hexagon.util

import org.slf4j.LoggerFactory.getLogger
import java.lang.System.*
import kotlin.reflect.KClass

open class CompanionLogger(clazz: KClass<out Any>){
    companion object {
        val FLARE_PREFIX = getProperty ("CompanionLogger.flarePrefix", ">>>>>>>>")
    }

    private val logger = getLogger(clazz.java)

    fun trace (message: String) = logger.trace(message)
    fun debug (message: String) = logger.debug(message)
    fun info (message: String) = logger.info(message)
    fun warn (message: String) = logger.warn(message)
    fun error (message: String) = logger.error(message)
    fun warn (message: String, exception: Throwable) = logger.warn(message, exception)
    fun error (message: String, exception: Throwable) = logger.error(message, exception)

    fun flare (message: String = "") = logger.trace("${FLARE_PREFIX} $message")
}
