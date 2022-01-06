package com.hexagonkt.core.handlers

import com.hexagonkt.core.logging.Logger

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {

    companion object {
        private val logger: Logger = Logger(Handler::class)
    }

    val predicate: Predicate<T>
    val callback: Callback<T>

    suspend fun process(context: Context<T>): Context<T> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            logger.info { "Exception processing handler callback: ${e.message}" }
            context.copy(exception = e)
        }

    suspend fun process(event: T): T =
        process(Context(event, predicate)).event
}
