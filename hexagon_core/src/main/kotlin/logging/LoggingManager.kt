package com.hexagonkt.logging

import com.hexagonkt.helpers.fail
import com.hexagonkt.injection.InjectionManager.injectOrNull
import com.hexagonkt.logging.jul.JulLoggingAdapter
import kotlin.reflect.KClass

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 */
object LoggingManager {
    var adapter: LoggingPort = injectOrNull() ?: JulLoggingAdapter

    fun setLoggerLevel(name: String, level: LoggingLevel) {
        adapter.setLoggerLevel(name, level)
    }

    fun setLoggerLevel(instance: Any, level: LoggingLevel) {
        setLoggerLevel(instance::class, level)
    }

    fun setLoggerLevel(type: KClass<*>, level: LoggingLevel) {
        setLoggerLevel(type.qualifiedName ?: fail, level)
    }

    fun setLoggerLevel(level: LoggingLevel) {
        setLoggerLevel("", level)
    }
}
