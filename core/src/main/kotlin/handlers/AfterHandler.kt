package com.hexagonkt.core.handlers

import com.hexagonkt.core.logging.Logger

/**
 * After handlers are executed even if a filter don't call next handler (if after was added before
 * filter).
 */
data class AfterHandler<T : Any>(
    val afterFilter: Predicate<T> = { true },
    val afterCallback: Callback<T>,
) : Handler<T> {

    // TODO Explain the paragraph below on API documentation
    // After handlers' filters are always true because they are meant to be evaluated on the return
    // If they are not called in first place, they won't be executed on the return of the next
    // handler
    override val predicate: Predicate<T> = { true }

    companion object {
        private val logger: Logger = Logger(AfterHandler::class)
    }

    override val callback: Callback<T> = {
        val next = it.next().copy(currentFilter = afterFilter)
        try {
            if (afterFilter.invoke(next)) afterCallback(next)
            else next
        }
        catch (e: Exception) {
            logger.info { "Exception processing after handler callback: ${e.message}" }
            next.copy(exception = e)
        }
    }
}
