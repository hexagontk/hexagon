package com.hexagonkt.handlers

data class OnHandler<T : Any>(
    override val predicate: (Context<T>) -> Boolean = { true },
    override val callback: (Context<T>) -> Context<T>,
) : Handler<T> {

    override fun process(context: Context<T>): Context<T> =
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
