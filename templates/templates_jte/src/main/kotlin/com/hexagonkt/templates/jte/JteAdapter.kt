package com.hexagonkt.templates.jte

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.templates.TemplatePort
import gg.jte.CodeResolver
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.TemplateOutput
import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.resolve.ResourceCodeResolver
import java.net.URL
import java.nio.file.Path
import java.util.*

class JteAdapter(
    mediaType: MediaType,
    resolverBase: URL? = null,
    precompiled: Boolean = false
) : TemplatePort {

    private companion object {
        val allowedTypes: String = setOf(TEXT_HTML, TEXT_PLAIN).joinToString(", ") { it.fullType }
    }

    private val contentType = when (mediaType) {
        TEXT_HTML -> ContentType.Html
        TEXT_PLAIN -> ContentType.Plain
        else ->
            error("Unsupported media type not in: $allowedTypes (${mediaType.fullType})")
    }

    private val resolver: CodeResolver =
        when (resolverBase?.protocol) {
            "classpath" -> ResourceCodeResolver(resolverBase.path)
            "file" -> DirectoryCodeResolver(Path.of(resolverBase.path))
            null -> ResourceCodeResolver("")
            else -> error("Invalid base schema not in: classpath, file (${resolverBase.protocol})")
        }

    private val templateEngine: TemplateEngine =
        if (precompiled) {
            if (resolverBase === null) {
                TemplateEngine.createPrecompiled(contentType)
            }
            else {
                val protocol = resolverBase.protocol
                check(protocol == "classpath") {
                    "Precompiled base must be classpath URLs ($protocol)"
                }
                TemplateEngine.createPrecompiled(Path.of(resolverBase.path), contentType)
            }
        }
        else {
            TemplateEngine.create(resolver, contentType)
        }

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String {
        val output: TemplateOutput = StringOutput()
        templateEngine.render(url.path, context, output)
        return output.toString()
    }

    override fun render(
        name: String, templates: Map<String, String>, context: Map<String, *>, locale: Locale
    ): String =
        throw UnsupportedOperationException("jte does not support memory templates")
}
