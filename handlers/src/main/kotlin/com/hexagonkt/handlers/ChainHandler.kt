package com.hexagonkt.handlers

data class ChainHandler<T : Any>(
    val handlers: List<Handler<T>>,
    override val predicate: Predicate<T> = { true },
) : Handler<T> {

    constructor(
        filter: Predicate<T>,
        vararg handlers: Handler<T>,
    ) : this(handlers.toList(), filter)

    constructor(vararg handlers: Handler<T>) : this(handlers.toList(), { true })

    override val callback: Callback<T> = {
        val nestedContext = it.with(event = it.event, nextHandlers = handlers, nextHandler = 0)
        val nestedResult = nestedContext.next()
        val followUpContext = nestedResult.with(
            currentFilter = predicate,
            nextHandlers = it.nextHandlers,
            nextHandler = it.nextHandler
        )
        followUpContext.next()
    }
}
