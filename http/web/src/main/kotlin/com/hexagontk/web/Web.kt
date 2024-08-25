package com.hexagontk.web

import com.hexagontk.core.media.mediaTypeOfOrNull
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.model.Cookie
import com.hexagontk.http.model.Headers
import com.hexagontk.templates.TemplateManager
import com.hexagontk.templates.TemplatePort
import java.net.URL
import java.nio.charset.Charset.defaultCharset
import java.util.Locale
import java.util.Locale.forLanguageTag as localeFor

fun HttpContext.templateType(url: URL): ContentType? =
    response.contentType ?: run {
        val mimeType = mediaTypeOfOrNull(url)
        mimeType?.let { ContentType(it, charset = defaultCharset()) }
    }

fun HttpContext.callContext(): Map<String, *> =
    mapOf(
        "_path_" to request.path.removeSuffix("/"), // Do not allow trailing slash
        "_lang_" to obtainLocale().language,
    )

/**
 * TODO Review order precedence and complete code (now only taking request attribute)
 *
 * 1. Request
 * 2. Accept-language
 * 3. Cookie
 * 4. Server default locale
 */
fun HttpContext.obtainLocale(): Locale = when {
    attributes["lang"] as? String != null -> localeFor(attributes["lang"] as String)
    else -> Locale.getDefault()
}

fun HttpContext.template(
    templateEngine: TemplatePort,
    url: URL,
    context: Map<String, *> = emptyMap<String, Any>(),
    locale: Locale = obtainLocale(),
    headers: Headers = response.headers,
    cookies: List<Cookie> = response.cookies,
    attributes: Map<*, *> = this.attributes,
): HttpContext =
    ok(
        templateEngine.render(url, callContext() + context, locale),
        headers,
        templateType(url),
        cookies,
        attributes,
    )

fun HttpContext.template(
    url: URL,
    context: Map<String, *> = emptyMap<String, Any>(),
    locale: Locale = obtainLocale(),
    headers: Headers = response.headers,
    cookies: List<Cookie> = response.cookies,
    attributes: Map<*, *> = this.attributes,
): HttpContext =
    ok(
        TemplateManager.render(url, callContext() + context, locale),
        headers,
        templateType(url),
        cookies,
        attributes,
    )
