package com.hexagontk.handlers

/**
 * Context for an event.
 *
 * @param T Event type.
 */
interface Context<T : Any> {
    var event: T
    var predicate: (Context<T>) -> Boolean
    var nextHandlers: Array<Handler<T>>
    var nextHandler: Int
    var exception: Exception?
    var attributes: Map<*, *>
    var handled: Boolean

    fun with(
        event: T = this.event,
        predicate: (Context<T>) -> Boolean = this.predicate,
        nextHandlers: Array<Handler<T>> = this.nextHandlers,
        nextHandler: Int = this.nextHandler,
        exception: Exception? = this.exception,
        attributes: Map<*, *> = this.attributes,
        handled: Boolean = this.handled,
    ): Context<T>

    fun next(): Context<T> {
        for (index in nextHandler until nextHandlers.size) {
            val handler = nextHandlers[index]
            val p = handler.predicate
            if (handler is OnHandler) {
                if ((!handled) && p(this))
                    return handler.process(with(predicate = p, nextHandler = index + 1))
            }
            else {
                if (p(this))
                    return handler.process(with(predicate = p, nextHandler = index + 1))
            }
        }

        return this
    }
}
