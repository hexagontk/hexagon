package com.hexagonkt.handlers

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {
    val predicate: Predicate<T>
    val callback: Callback<T>

    fun process(context: Context<T>): Context<T> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            context.with(exception = e)
        }
}
