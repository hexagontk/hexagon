package com.hexagonkt.web

import com.hexagonkt.helpers.logger
import java.util.Locale.forLanguageTag as localeFor

import com.hexagonkt.http.server.Call
import com.hexagonkt.serialization.SerializationManager
import java.nio.charset.Charset.defaultCharset
import java.util.*
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.templates.TemplateManager.render
import com.hexagonkt.templates.TemplatePort

fun Call.templateType(template: String) {
    if (response.contentType == null) {
        val mimeType = SerializationManager.contentTypeOf(template.substringAfterLast('.'))
        response.contentType = "$mimeType; charset=${defaultCharset().name()}"
        logger.trace { response }
        logger.trace { response.contentType }
    }
}

fun Call.fullContext(): Map<String, *> {
    val extraParameters = mapOf(
        "path" to request.path.removeSuffix("/"), // Do not allow trailing slash
        "lang" to obtainLocale().language
    )
    logger.trace { extraParameters }

    return settings + session.attributes + extraParameters
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
) {

    templateType(templateName)
    ok(render(templateAdapter, templateName, locale, context))
}
