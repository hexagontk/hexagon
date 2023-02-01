package com.hexagonkt.handlers

/**
 * Context for an event.
 *
 * @param T Event type.
 */
data class EventContext<T : Any>(
    override val event: T,
    override val predicate: Predicate<T>,
    override val nextHandlers: List<Handler<T>> = emptyList(),
    override val nextHandler: Int = 0,
    override val exception: Exception? = null,
    override val attributes: Map<*, *> = emptyMap<Any, Any>(),
) : Context<T> {

    override fun with(
        event: T,
        predicate: Predicate<T>,
        nextHandlers: List<Handler<T>>,
        nextHandler: Int,
        exception: Exception?,
        attributes: Map<*, *>,
    ): EventContext<T> =
        copy(
            event = event,
            predicate = predicate,
            nextHandlers = nextHandlers,
            nextHandler = nextHandler,
            exception = exception,
            attributes = attributes,
        )

    override fun next(): Context<T> {
        for (index in nextHandler until nextHandlers.size) {
            val handler = nextHandlers[index]
            if (handler.predicate(this))
                return handler.process(
                    with(predicate = handler.predicate, nextHandler = index + 1)
                )
        }

        return this
    }
}
