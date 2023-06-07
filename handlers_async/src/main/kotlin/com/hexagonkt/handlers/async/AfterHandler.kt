package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture

/**
 * After handlers are executed even if a filter don't call next handler (if after was added before
 * filter).
 *
 * After handlers' filters are always true because they are meant to be evaluated on the **return**.
 * If they are not called in first place, they won't be executed on the return of the next handler.
 * Their filter is evaluated after the `next` call, not before.
 */
data class AfterHandler<T : Any>(
    val afterPredicate: (Context<T>) -> Boolean = { true },
    override val callback: (Context<T>) -> CompletableFuture<Context<T>>,
) : Handler<T> {

    override val predicate: (Context<T>) -> Boolean = { true }

    override fun process(context: Context<T>): CompletableFuture<Context<T>> {
        return context.next().thenCompose { c ->
            val next = c.with(predicate = afterPredicate)
            try {
                if (afterPredicate.invoke(next)) callback(next)
                else CompletableFuture.completedFuture(next)
            }
            catch (e: Exception) {
                CompletableFuture.completedFuture(next.with(exception = e))
            }
        }
    }
}
