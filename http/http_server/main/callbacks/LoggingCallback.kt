package com.hexagontk.http.server.callbacks

import com.hexagontk.core.loggerOf
import com.hexagontk.http.model.*
import com.hexagontk.http.handlers.HttpContext
import java.lang.System.Logger
import java.lang.System.Logger.Level
import kotlin.system.measureNanoTime

/**
 * Callback that logs server requests and responses.
 */
class LoggingCallback(
    private val level: Level = Level.INFO,
    private val logger: Logger = loggerOf(LoggingCallback::class),
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
        val result = "${m.status}"
        val time = "(${ns / 10e5} ms)"
        val body = m.formatBody()
        return "$path -> $result $time$headers$body".trim()
    }

    private fun HttpMessage.formatBody(): String =
        if (includeBody) "\n\n${bodyString()}" else ""

    private fun Headers.format(): String =
        all
            .filter { (_, v) -> v.any { it.text.isNotBlank() } }
            .map { (k, v) -> "$k: ${v.joinToString(", ") { it.text}}" }
            .joinToString("\n", prefix = "\n\n")
}
