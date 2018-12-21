package com.hexagonkt.web

import java.util.Locale.forLanguageTag as localeFor

import com.hexagonkt.http.server.Call
import com.hexagonkt.serialization.SerializationManager
import java.nio.charset.Charset.defaultCharset
import java.util.*
import com.hexagonkt.settings.SettingsManager.settings

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
