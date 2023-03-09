package com.hexagonkt.handlers

data class ChainHandler<T : Any>(
    val handlers: List<Handler<T>>,
    override val predicate: (Context<T>) -> Boolean = { true },
) : Handler<T> {

    override val callback: (Context<T>) -> Context<T> = { it }

    constructor(
        filter: (Context<T>) -> Boolean,
        vararg handlers: Handler<T>,
    ) : this(handlers.toList(), filter)

    constructor(vararg handlers: Handler<T>) : this(handlers.toList(), { true })

    override fun process(context: Context<T>): Context<T> {
        val event = context.event
        val nestedContext = context.with(event = event, nextHandlers = handlers, nextHandler = 0)
        val nestedResult = nestedContext.next()
        val followUpContext = nestedResult.with(
            predicate = predicate,
            nextHandlers = context.nextHandlers,
            nextHandler = context.nextHandler
        )
        return followUpContext.next()
    }
}
