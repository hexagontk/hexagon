package com.hexagonkt.handlers

/**
 * Context for an event.
 *
 * @param T Event type.
 */
interface Context<T : Any> {
    val event: T
    val predicate: Predicate<T>
    val nextHandlers: List<Handler<T>>
    val nextHandler: Int
    val exception: Exception?
    val attributes: Map<*, *>

    fun with(
        event: T = this.event,
        predicate: Predicate<T> = this.predicate,
        nextHandlers: List<Handler<T>> = this.nextHandlers,
        nextHandler: Int = this.nextHandler,
        exception: Exception? = this.exception,
        attributes: Map<*, *> = this.attributes,
    ): Context<T>

    fun next(): Context<T>
}
