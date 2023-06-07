package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture

data class FilterHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val callback: (Context<T>) -> CompletableFuture<Context<T>>,
) : Handler<T> {

    override fun process(context: Context<T>): CompletableFuture<Context<T>> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            CompletableFuture.completedFuture(context.with(exception = e))
        }
}
