package com.hexagonkt.core.handlers

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
        val nestedResult = it.copy(event = it.event, nextHandlers = handlers).next()
        nestedResult.copy(currentFilter = predicate, nextHandlers = it.nextHandlers).next()
    }
}
