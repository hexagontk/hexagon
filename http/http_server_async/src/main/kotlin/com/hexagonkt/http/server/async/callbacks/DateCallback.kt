package com.hexagonkt.http.server.async.callbacks

import com.hexagonkt.http.model.Header
import com.hexagonkt.http.handlers.async.HttpContext
import com.hexagonkt.http.toHttpFormat
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Callback that adds the `date` header to the response (caching its value).
 */
class DateCallback(private val name: String = "date") :
    (HttpContext) -> CompletableFuture<HttpContext> {

    private var lastUpdate: AtomicLong = AtomicLong(0)
    private var date: AtomicReference<String> = AtomicReference("")

    override fun invoke(context: HttpContext): CompletableFuture<HttpContext> {
        val ms = System.currentTimeMillis()

        if (lastUpdate.get() + 1_000 <= ms) {
            lastUpdate.set(ms - (ms % 1_000))
            date.set(Instant.ofEpochMilli(lastUpdate.get()).toHttpFormat())
        }

        return context
            .send(headers = context.response.headers + Header(name, date.get()))
            .next()
            .thenApply { it as HttpContext }
    }
}
