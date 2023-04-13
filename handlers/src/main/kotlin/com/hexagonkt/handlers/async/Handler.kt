package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
interface Handler<T : Any> {
    val predicate: (Context<T>) -> Boolean
    val callback: (Context<T>) -> CompletableFuture<Context<T>>

    fun process(context: Context<T>): CompletableFuture<Context<T>>
}
