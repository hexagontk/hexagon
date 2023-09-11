package com.hexagonkt.handlers.coroutines

data class FilterHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val callback: suspend (Context<T>) -> Context<T>,
) : Handler<T> {

    override suspend fun process(context: Context<T>): Context<T> =
        try {
            callback(context)
        }
        catch (e: Exception) {
            context.with(exception = e)
        }
}
