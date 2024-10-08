package com.hexagontk.handlers

data class ChainHandler<T : Any>(
    private val rawHandlers: List<Handler<T>>,
    override val predicate: (Context<T>) -> Boolean = { true },
    override val parent: Handler<T>? = null,
) : Handler<T> {

    val handlers: List<Handler<T>> =
        rawHandlers.map {
            when (it) {
                is AfterHandler -> it.copy(parent = this)
                is BeforeHandler -> it.copy(parent = this)
                is ChainHandler -> it.copy(parent = this)
                is ExceptionHandler<T, *> -> it.copy(parent = this)
                is FilterHandler -> it.copy(parent = this)
                is OnHandler -> it.copy(parent = this)
                else -> it
            }
        }

    constructor(
        filter: (Context<T>) -> Boolean,
        vararg handlers: Handler<T>,
    ) : this(handlers.toList(), filter, null)

    constructor(vararg handlers: Handler<T>) : this(handlers.toList(), { true })

    override fun process(context: Context<T>): Context<T> {
        val nestedContext = context.with(nextHandlers = handlers, nextHandler = 0)
        val nestedResult = nestedContext.next()
        val followUpContext = nestedResult.with(
            predicate = predicate,
            nextHandlers = context.nextHandlers,
            nextHandler = context.nextHandler
        )
        return followUpContext.next()
    }
}
