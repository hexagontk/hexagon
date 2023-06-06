package com.hexagonkt.http.server.async.callbacks

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.http.model.*
import com.hexagonkt.http.handlers.async.HttpContext
import java.util.concurrent.CompletableFuture
import java.lang.System.nanoTime

/**
 * Callback that logs server requests and responses.
 */
class LoggingCallback(
    private val level: LoggingLevel = LoggingLevel.INFO,
    private val logger: Logger = Logger(LoggingCallback::class),
    private val includeHeaders: Boolean = false,
    private val includeBody: Boolean = true,
) : (HttpContext) -> CompletableFuture<HttpContext> {

    override fun invoke(context: HttpContext): CompletableFuture<HttpContext> {
        logger.log(level) { details(context.request) }
        val start = nanoTime()
        return context.send().next().thenApply {
            val serverContext = it as HttpContext
            val ns = nanoTime() - start
            logger.log(level) { details(context.request, serverContext.response, ns) }
            serverContext
        }
    }

    internal fun details(m: HttpRequestPort): String {
        val headers = if (includeHeaders) {
            val accept = Header("accept", m.accept.joinToString(", ") { it.text })
            val contentType = Header("content-type", m.contentType?.text ?: "")
            (m.headers - "accept" - "content-type" + accept + contentType).format()
        }
        else {
            ""
        }

        val body = m.formatBody()
        return "Request:\n${m.method} ${m.path}$headers$body".trim()
    }

    internal fun details(n: HttpRequestPort, m: HttpResponsePort, ns: Long): String {
        val headers = if (includeHeaders) {
            val contentType = Header("content-type", m.contentType?.text ?: "")
            (m.headers - "content-type" + contentType).format()
        } else {
            ""
        }

        val path = "${n.method} ${n.path}"
        val result = "${m.status.type}(${m.status.code})"
        val time = "(${ns / 10e5} ms)"
        val body = m.formatBody()
        return "Response (partial headers):\n$path -> $result $time$headers$body".trim()
    }

    private fun HttpMessage.formatBody(): String =
        if (includeBody) "\n\n${bodyString()}" else ""

    private fun Headers.format(): String =
        httpFields
            .filter { (_, v) -> v.strings().any { it.isNotBlank() } }
            .map { (k, v) -> "$k: ${v.strings().joinToString(", ")}" }
            .joinToString("\n", prefix = "\n\n")
}
