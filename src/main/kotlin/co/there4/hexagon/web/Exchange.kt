package co.there4.hexagon.web

import co.there4.hexagon.repository.FileRepository.load
import co.there4.hexagon.template.PebbleRenderer.render
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML
import java.nio.charset.Charset.defaultCharset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.Locale.forLanguageTag as localeFor

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
data class Exchange (
    val request: Request,
    val response: Response,
    val session: Session,
    /** Exchange attributes (for the current request). Same as HttpServletRequest.setAttribute(). */
    val attributes: MutableMap<String, Any> = mutableMapOf<String, Any>()) {

    fun redirect(url: String) = response.redirect(url)

    fun ok(content: Any, type: String? = null) = send(200, content, type)
    fun created(content: Any, type: String? = null) = send(201, content, type)
    fun ok(code: Int = 200, content: Any = "", type: String? = null) = send(code, content, type)
    fun error(code: Int = 500, content: Any = "") = send(code, content)

    fun halt(content: Any): Nothing = halt(500, content)
    fun halt(code: Int = 500, content: Any = ""): Nothing {
        send(code, content)
        throw EndException()
    }

    fun pass(): Nothing = throw PassException()

    fun template(
        template: String,
        locale: Locale = obtainLocale(),
        context: Map<String, *> = mapOf<String, Any>()) {

        val contentType = response.getMimeType(template)

        if (response.contentType == null)
            response.contentType = "$contentType; charset=${defaultCharset().name()}"

        val extraParameters = mapOf(
            "pathInfo" to request.pathInfo.removeSuffix("/"), // Do not allow trailing slash
            "lang" to locale.language
        )

        ok(render(template, locale, context + session.attributes + extraParameters))
    }

    fun template(template: String, context: Map<String, *> = mapOf<String, Any>()) {
        template(template, obtainLocale(), context)
    }

    fun template(template: String, vararg context: Pair<String, *>) =
        template(template, context.toMap())

    fun page(callback: TagConsumer<String>.() -> String) {
        val html = createHTML().callback()
        response.contentType = "text/html; charset=${defaultCharset().name()}"
        ok("<!DOCTYPE html>\n\n$html")
    }

    fun file(name: String) {
        val meta = load(name, response.outputStream)
        response.contentType = meta["Content-Type"].toString()
        response.outputStream.flush()
        response.status = 200
    }

    /**
     * TODO Review order precedence and complete code (now only taking request attribute)
     *
     * 1. Request
     * 2. Session
     * 3. Cookie
     * 4. Accept-language
     * 5. Server default locale
     */
    private fun obtainLocale() = when {
        attributes["lang"] as? String != null -> localeFor(attributes["lang"] as String)
        else -> Locale.getDefault()
    }

    fun httpDate (date: LocalDateTime): String =
        RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))

    private fun send(code: Int, content: Any, contentType: String? = null) {
        response.status = code
        response.body = content

        if (contentType != null)
            response.contentType =
                if(contentType.contains("charset")) contentType
                else "$contentType; charset=${defaultCharset().name()}"
    }
}
