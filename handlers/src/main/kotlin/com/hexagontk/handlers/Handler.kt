package com.hexagontk.handlers

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {
    val parent: Handler<T>?
    val predicate: (Context<T>) -> Boolean
    val callback: (Context<T>) -> Context<T>

    fun process(context: Context<T>): Context<T>
}
