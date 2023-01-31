package com.hexagonkt.handlers

/**
 * After handlers are executed even if a filter don't call next handler (if after was added before
 * filter).
 *
 * After handlers' filters are always true because they are meant to be evaluated on the **return**.
 * If they are not called in first place, they won't be executed on the return of the next handler.
 * Their filter is evaluated after the `next` call, not before.
 */
data class AfterHandler<T : Any>(
    val afterPredicate: Predicate<T> = { true },
    val afterCallback: Callback<T>,
) : Handler<T> {

    override val predicate: Predicate<T> = { true }

    override val callback: Callback<T> = {
        val next = it.next().with(predicate = afterPredicate)
        try {
            if (afterPredicate.invoke(next)) afterCallback(next)
            else next
        }
        catch (e: Exception) {
            next.with(exception = e)
        }
    }
}
