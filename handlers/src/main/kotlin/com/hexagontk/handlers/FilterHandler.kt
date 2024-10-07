package com.hexagontk.handlers

data class FilterHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val parent: Handler<T>? = null,
    override val callback: (Context<T>) -> Context<T>,
) : Handler<T> {

    override fun process(context: Context<T>): Context<T> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            context.with(exception = e)
        }
}
