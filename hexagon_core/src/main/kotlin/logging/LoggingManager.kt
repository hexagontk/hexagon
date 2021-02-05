package com.hexagonkt.logging

import com.hexagonkt.injection.InjectionManager.injectOrNull
import com.hexagonkt.logging.jul.JulLoggingAdapter

/**
 * TODO
 *   - Rename `logging_slf4j` to `logging_logback`
 *   - Create `logging_slf4j_simple` to `logging_slf4j_jul`
 *   - Update documentation
 */
object LoggingManager {
    var adapter: LoggingPort = injectOrNull() ?: JulLoggingAdapter

    fun setLoggerLevel(name: String, level: LoggingLevel) {
        adapter.setLoggerLevel(name, level)
    }
}
