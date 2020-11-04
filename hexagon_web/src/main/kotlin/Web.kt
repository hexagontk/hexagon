package com.hexagonkt.web

import com.hexagonkt.http.server.Call
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.templates.TemplateEngine
import com.hexagonkt.templates.TemplatePort
import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import java.nio.charset.Charset.defaultCharset
import java.util.Locale
import java.util.Locale.forLanguageTag as localeFor

fun Call.templateType(template: String) {
    if (response.contentType == null) {
        val mimeType = SerializationManager.contentTypeOf(template.substringAfterLast('.'))
        response.contentType = "$mimeType; charset=${defaultCharset().name()}"
    }
}

fun Call.fullContext(): Map<String, *> {
    val extraParameters = mapOf(
        "path" to request.path.removeSuffix("/"), // Do not allow trailing slash
        "lang" to obtainLocale().language
    )

    return session.attributes + extraParameters
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
fun Call.obtainLocale(): Locale = when {
    attributes["lang"] as? String != null -> localeFor(attributes["lang"] as String)
    else -> Locale.getDefault()
}

fun Call.template(
    templateAdapter: TemplatePort,
    templateName: String,
    locale: Locale = obtainLocale(),
    context: Map<String, *> = fullContext()
) = template(TemplateEngine(templateAdapter), templateName, locale, context)

fun Call.template(
    templateEngine: TemplateEngine,
    templateName: String,
    locale: Locale = obtainLocale(),
    context: Map<String, *> = fullContext()
) {

    templateType(templateName)
    ok(templateEngine.render(templateName, locale, context))
}

/**
 * Return HTML setting the proper content type.
 */
fun Call.html(block: HTML.() -> Unit) {
    ok(createHTML().html { block() }, "text/html")
}
