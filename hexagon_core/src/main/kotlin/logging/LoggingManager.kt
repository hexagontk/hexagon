package com.hexagonkt.logging

import com.hexagonkt.logging.jul.JulLoggingAdapter

object LoggingManager {
    var adapter: LoggingPort = JulLoggingAdapter
}
