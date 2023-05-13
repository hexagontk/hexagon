package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

data class ChainHandler<T : Any>(
    val handlers: List<Handler<T>>,
    override val predicate: (Context<T>) -> Boolean = { true },
) : Handler<T> {

    override val callback: (Context<T>) -> CompletableFuture<Context<T>> = { completedFuture(it) }

    constructor(
        filter: (Context<T>) -> Boolean,
        vararg handlers: Handler<T>,
    ) : this(handlers.toList(), filter)

    constructor(vararg handlers: Handler<T>) : this(handlers.toList(), { true })

    override fun process(context: Context<T>): CompletableFuture<Context<T>> {
        val event = context.event
        val nestedContext = context.with(event = event, nextHandlers = handlers, nextHandler = 0)
        return nestedContext.next().thenCompose { c ->
            val followUpContext = c.with(
                predicate = context.predicate,
                nextHandlers = context.nextHandlers,
                nextHandler = context.nextHandler
            )
            followUpContext.next()
        }
    }
}
