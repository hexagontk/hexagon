package co.there4.hexagon.template

import co.there4.hexagon.web.Exchange
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML
import java.nio.charset.Charset.defaultCharset
import java.lang.ClassLoader.getSystemResourceAsStream as resourceAsStream

/**
 * TODO Support different engines by subdir. Ie:
 * TODO Add code to test templates (check unresolved variables in bundles, multilanguage, etc.)
 *
 * templates/pebble/file
 * templates/freemarker/file
 * ...
 */
internal object KotlinxHtmlRenderer {
    fun Exchange.page(callback: TagConsumer<String>.() -> String) {
        val html = createHTML().callback()
        response.contentType = "text/html; charset=${defaultCharset().name()}"
        ok("<!DOCTYPE html>\n\n$html")
    }
}
