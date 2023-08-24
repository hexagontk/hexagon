package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.http.model.*
import com.hexagonkt.http.handlers.HttpContext
import kotlin.system.measureNanoTime

/**
 * Callback that logs server requests and responses.
 */
class LoggingCallback(
    private val level: LoggingLevel = LoggingLevel.INFO,
    private val logger: Logger = Logger(LoggingCallback::class),
    private val includeHeaders: Boolean = false,
    private val includeBody: Boolean = true,
) : (HttpContext) -> HttpContext {

    override fun invoke(context: HttpContext): HttpContext {
        var result: HttpContext

        logger.log(level) { details(context.request) }
        val ns = measureNanoTime { result = context.next() }
        logger.log(level) { details(context.request, result.response, ns) }

        return result
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
        return "${m.method} ${m.path}$headers$body".trim()
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
        return "$path -> $result $time$headers$body".trim()
    }

    private fun HttpMessage.formatBody(): String =
        if (includeBody) "\n\n${bodyString()}" else ""

    private fun Headers.format(): String =
        httpFields
            .filter { (_, v) -> v.strings().any { it.isNotBlank() } }
            .map { (k, v) -> "$k: ${v.strings().joinToString(", ")}" }
            .joinToString("\n", prefix = "\n\n")
}
