package com.hexagonkt.handlers.coroutines

data class OnHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val callback: suspend (Context<T>) -> Context<T>,
) : Handler<T> {

    override suspend fun process(context: Context<T>): Context<T> =
        try {
            val callbackContext = callback(context)
            if (callbackContext.handled)
                callbackContext.next()
            else
                callbackContext.with(handled = true).next()
        }
        catch (e: Exception) {
            context.with(exception = e).next()
        }
}
