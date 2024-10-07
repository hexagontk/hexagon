package com.hexagontk.http.handlers

import com.hexagontk.handlers.Context
import com.hexagontk.http.model.HttpCall

/**
 * Utility to encapsulate a handler in a class. TODO
 */
interface HttpController : HttpHandler {
    val handler: HttpHandler

    override val parent: HttpHandler?
        get() = handler.parent

    override val handlerPredicate: HttpPredicate
        get() = handler.handlerPredicate

    override fun addPrefix(prefix: String): HttpHandler =
        handler.addPrefix(prefix)

    override fun process(context: Context<HttpCall>): Context<HttpCall> =
        handler.process(context)

    override val predicate: (Context<HttpCall>) -> Boolean
        get() = handler.predicate

    override val callback: (Context<HttpCall>) -> Context<HttpCall>
        get() = handler.callback
}
