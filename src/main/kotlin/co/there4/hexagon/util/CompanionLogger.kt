package co.there4.hexagon.util

import org.slf4j.LoggerFactory.getLogger
import java.lang.System.*
import kotlin.reflect.KClass

open class CompanionLogger(clazz: KClass<out Any>){
    companion object {
        val FLARE_PREFIX = getProperty ("CompanionLogger.flarePrefix", ">>>>>>>>")
    }

    private val logger = getLogger(clazz.java)

    fun trace (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        logger.trace(message, arguments)

    fun debug (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        logger.debug(message, arguments)

    fun info (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        logger.info(message, arguments)

    fun warn (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        logger.warn(message, arguments)

    fun error (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        logger.error(message, arguments)

    fun warn (message: String, exception: Throwable) = logger.warn(message, exception)

    fun error (message: String, exception: Throwable) = logger.error(message, exception)

    fun flare (message: String = "") = logger.trace("${FLARE_PREFIX} $message")
}
