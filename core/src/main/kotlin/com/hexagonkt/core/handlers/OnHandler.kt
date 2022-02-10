package com.hexagonkt.core.handlers

import com.hexagonkt.core.logging.Logger

data class OnHandler<T : Any>(
    override val predicate: Predicate<T> = { true },
    val beforeCallback: Callback<T>,
) : Handler<T> {

    companion object {
        private val logger: Logger = Logger(OnHandler::class)
    }

    override val callback: Callback<T> = {
        try {
            beforeCallback(it).next()
        }
        catch (e: Exception) {
            logger.info { "Exception processing before handler callback: ${e.message}" }
            it.copy(exception = e).next()
        }
    }
}
