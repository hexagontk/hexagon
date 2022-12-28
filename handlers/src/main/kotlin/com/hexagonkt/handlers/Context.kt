package com.hexagonkt.handlers

/**
 * Context for an event.
 *
 * @param T Event type.
 */
data class Context<T : Any>(
    val event: T,
    val currentFilter: Predicate<T>,
    val nextHandlers: List<Handler<T>> = emptyList(),
    val exception: Exception? = null,
    val attributes: Map<*, *> = emptyMap<Any, Any>(),
) {

    fun next(): Context<T> {
        val matchingHandlers = nextHandlers.dropWhile { !it.predicate(this) }
        val nextHandler = matchingHandlers.firstOrNull()
        return nextHandler
            ?.process(
                copy(currentFilter = nextHandler.predicate, nextHandlers = matchingHandlers.drop(1))
            )
            ?: this
    }
}
