package com.hexagontk.handlers

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {
    val parent: Handler<T>?
    val predicate: (Context<T>) -> Boolean

    /**
     * How the Handler implementation should call the callback and take care of handlers chain,
     * exceptions, etc.
     */
    fun process(context: Context<T>): Context<T>
}
