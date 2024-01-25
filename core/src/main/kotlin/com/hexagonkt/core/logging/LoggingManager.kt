package com.hexagonkt.core.logging

/**
 * Manages Logs.
 */
object LoggingManager {
    var useColor: Boolean = true
    var defaultLoggerName: String = "com.hexagonkt.core.logging"
        set(value) {
            require(value.isNotEmpty()) { "Default logger name cannot be empty string" }
            field = value
        }
}
