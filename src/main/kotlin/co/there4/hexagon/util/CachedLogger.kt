package co.there4.hexagon.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import kotlin.reflect.KClass

open class CachedLogger(type: KClass<out Any>) : Loggable {
    val logger: Logger by lazy { getLogger(type.java) }

    override fun logger(): Logger = logger
}
