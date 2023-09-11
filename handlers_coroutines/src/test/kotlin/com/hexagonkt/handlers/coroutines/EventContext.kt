package com.hexagonkt.handlers.coroutines

data class EventContext<T : Any>(
    override val event: T,
    override val predicate: (Context<T>) -> Boolean,
    override val nextHandlers: List<Handler<T>> = emptyList(),
    override val nextHandler: Int = 0,
    override val exception: Exception? = null,
    override val attributes: Map<*, *> = emptyMap<Any, Any>(),
    override val handled: Boolean = false,
) : Context<T> {

    override fun with(
        event: T,
        predicate: (Context<T>) -> Boolean,
        nextHandlers: List<Handler<T>>,
        nextHandler: Int,
        exception: Exception?,
        attributes: Map<*, *>,
        handled: Boolean,
    ): EventContext<T> =
        copy(
            event = event,
            predicate = predicate,
            nextHandlers = nextHandlers,
            nextHandler = nextHandler,
            exception = exception,
            attributes = attributes,
            handled = handled,
        )
}
