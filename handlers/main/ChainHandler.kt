package com.hexagontk.handlers

class ChainHandler<T : Any>(
    val handlers: Array<Handler<T>>,
    override val predicate: (Context<T>) -> Boolean = { true },
) : Handler<T> {

    constructor(
        handlers: List<Handler<T>>,
        filter: (Context<T>) -> Boolean = { true },
    ) : this(handlers.toTypedArray(), filter)

    constructor(
        filter: (Context<T>) -> Boolean,
        vararg handlers: Handler<T>,
    ) : this(handlers as Array<Handler<T>>, filter)

    constructor(vararg handlers: Handler<T>) : this(handlers as Array<Handler<T>>, { true })

    override fun process(context: Context<T>): Context<T> {
        val nextHandlers = context.nextHandlers
        val nextHandler = context.nextHandler

        val nestedContext = context.with(nextHandlers = handlers, nextHandler = 0)
        val nestedResult = nestedContext.next()

        val followUpContext = nestedResult.with(
            predicate = predicate,
            nextHandlers = nextHandlers,
            nextHandler = nextHandler
        )

        return followUpContext.next()
    }
}
