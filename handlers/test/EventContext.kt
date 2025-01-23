package com.hexagontk.handlers

class EventContext<T : Any>(
    override var event: T,
    override var predicate: (Context<T>) -> Boolean,
    override var nextHandlers: Array<Handler<T>> = emptyArray<Handler<T>>(),
    override var nextHandler: Int = 0,
    override var exception: Exception? = null,
    override var attributes: Map<*, *> = emptyMap<Any, Any>(),
    override var handled: Boolean = false,
) : Context<T> {

    override fun with(
        event: T,
        predicate: (Context<T>) -> Boolean,
        nextHandlers: Array<Handler<T>>,
        nextHandler: Int,
        exception: Exception?,
        attributes: Map<*, *>,
        handled: Boolean,
    ): EventContext<T> =
        apply {
            this.event = event
            this.predicate = predicate
            this.nextHandlers = nextHandlers
            this.nextHandler = nextHandler
            this.exception = exception
            this.attributes = attributes
            this.handled = handled
        }
}
