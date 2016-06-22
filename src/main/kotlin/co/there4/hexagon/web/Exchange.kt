package co.there4.hexagon.web

import co.there4.hexagon.template.PebbleRenderer.render
import java.nio.charset.Charset.defaultCharset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
data class Exchange (
    val request: Request,
    val response: Response,
    val session: Session) {

    fun redirect (url: String) = response.redirect(url)

    fun ok(content: Any) = send (200, content)
    fun ok(code: Int = 200, content: Any = "") = send (code, content)
    fun error(code: Int = 500, content: Any = "") = send (code, content)

    fun halt(content: Any) = halt (500, content)
    fun halt(code: Int = 500, content: Any = "") {
        send (code, content)
        throw EndException ()
    }

    fun pass() { throw PassException() }

    fun template (
        template: String,
        locale: Locale = Locale.getDefault(),
        context: Map<String, *> = mapOf<String, Any> ()) {

        val contentType = response.getMimeType(template)

        if (response.contentType == null)
            response.contentType = "$contentType; charset=${defaultCharset().name()}"
        ok (render (template, locale, context + ("lang" to locale.language)))
    }

    fun template (template: String, context: Map<String, *> = mapOf<String, Any> ()) {
        template (template, Locale.getDefault(), context)
    }

    fun httpDate (date: LocalDateTime) =
        RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))

    private fun send(code: Int, content: Any) {
        response.status = code
        response.body = content
    }
}
