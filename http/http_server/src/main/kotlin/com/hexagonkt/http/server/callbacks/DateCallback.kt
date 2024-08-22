package com.hexagontk.http.server.callbacks

import com.hexagontk.http.model.Header
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.toHttpFormat
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Callback that adds the `date` header to the response (caching its value).
 */
class DateCallback(private val name: String = "date") : (HttpContext) -> HttpContext {

    private var lastUpdate: AtomicLong = AtomicLong(0)
    private var date: AtomicReference<String> = AtomicReference("")

    override fun invoke(context: HttpContext): HttpContext {
        val ms = System.currentTimeMillis()

        if (lastUpdate.get() + 1_000 <= ms) {
            lastUpdate.set(ms - (ms % 1_000))
            date.set(Instant.ofEpochMilli(lastUpdate.get()).toHttpFormat())
        }

        return context.send(headers = context.response.headers + Header(name, date.get())).next()
    }
}
