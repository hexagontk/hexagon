package com.hexagontk.handlers

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * After handlers are executed even if a filter don't call next handler (if after was added before
 * filter).
 *
 * After handlers' filters are always true because they are meant to be evaluated on the **return**.
 * If they are not called in first place, they won't be executed on the return of the next handler.
 * Their filter is evaluated after the `next` call, not before.
 */
class ExceptionHandler<T : Any, E : Exception>(
    val exception: KClass<E>,
    val exceptionCallback: (Context<T>, E) -> Context<T>,
) : Handler<T> {

    internal companion object {
        fun <T : Exception> castException(exception: Exception?, exceptionClass: KClass<T>): T =
            exception
                ?.let { exceptionClass.cast(exception) }
                ?: error("Exception 'null' or incorrect type")
    }

    override val predicate: (Context<T>) -> Boolean = { true }
    val callback: (Context<T>) -> Context<T> = { context ->
        exceptionCallback(context, castException(context.exception, exception))
            .with(exception = null)
    }

    override fun process(context: Context<T>): Context<T> {
        val next = context.next().with(predicate = ::afterPredicate)
        return try {
            if (afterPredicate(next)) callback(next)
            else next
        }
        catch (e: Exception) {
            next.with(exception = e)
        }
    }

    private fun afterPredicate(context: Context<T>): Boolean {
        val exceptionClass = context.exception?.javaClass ?: return false
        return exception.java.isAssignableFrom(exceptionClass)
    }
}
