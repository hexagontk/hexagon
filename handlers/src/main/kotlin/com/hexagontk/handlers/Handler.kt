package com.hexagontk.handlers

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {
    val predicate: (Context<T>) -> Boolean

    fun process(context: Context<T>): Context<T>

    // TODO Uncomment this and add 'EventContext' class
//    fun context(event: T): Context<T>
//
//    fun process(event: T): Context<T> =
//        process(context(event))
//    fun wrap(predicate:, wrapper: Callback): Handler<T>
}
