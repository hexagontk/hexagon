package com.hexagonkt.handlers.coroutines

/**
 * Context for an event.
 *
 * @param T Event type.
 */
interface Context<T : Any> {
    val event: T
    val predicate: (Context<T>) -> Boolean
    val nextHandlers: List<Handler<T>>
    val nextHandler: Int
    val exception: Exception?
    val attributes: Map<*, *>
    val handled: Boolean

    fun with(
        event: T = this.event,
        predicate: (Context<T>) -> Boolean = this.predicate,
        nextHandlers: List<Handler<T>> = this.nextHandlers,
        nextHandler: Int = this.nextHandler,
        exception: Exception? = this.exception,
        attributes: Map<*, *> = this.attributes,
        handled: Boolean = this.handled,
    ): Context<T>

    suspend fun next(): Context<T> {
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
