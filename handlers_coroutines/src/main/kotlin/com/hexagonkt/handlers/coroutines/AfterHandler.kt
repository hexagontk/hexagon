package com.hexagonkt.handlers.coroutines

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
    override val callback: suspend (Context<T>) -> Context<T>,
) : Handler<T> {

    override val predicate: (Context<T>) -> Boolean = { true }

    override suspend fun process(context: Context<T>): Context<T> {
        val next = context.next().with(predicate = afterPredicate)
        return try {
            if (afterPredicate.invoke(next)) callback(next)
            else next
        }
        catch (e: Exception) {
            next.with(exception = e)
        }
    }
}
