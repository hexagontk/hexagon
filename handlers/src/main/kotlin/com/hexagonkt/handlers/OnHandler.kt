package com.hexagonkt.handlers

data class OnHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val callback: (Context<T>) -> Context<T>,
) : Handler<T> {

    override fun process(context: Context<T>): Context<T> =
        try {
            callback(context).next()
        }
        catch (e: Exception) {
            context.with(exception = e).next()
        }
}
