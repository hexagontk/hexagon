package com.hexagontk.handlers

class FilterHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    val callback: (Context<T>) -> Context<T>,
) : Handler<T> {

    override fun process(context: Context<T>): Context<T> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            context.with(exception = e)
        }
}
