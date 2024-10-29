package com.hexagontk.rest.tools

import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.model.HttpCall

/**
 * Callback that records server requests and responses (the whole event context). The result is
 * taken before any subsequent filter is applied (just how it was received).
 */
class RecordCallback : (HttpContext) -> HttpContext {

    var calls: List<HttpContext> = emptyList()

    override fun invoke(context: HttpContext): HttpContext {

        val result = context.next()
        calls += context
            .with(event = HttpCall(result.event.request, response = result.response)) as HttpContext

        return result
    }
}
