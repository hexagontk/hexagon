package co.there4.hexagon.rest.ratpack

import com.mitchellbosecke.pebble.PebbleEngine
import java.io.StringWriter
import java.util.*

/**
 * TODO Support different engines by subdir. Ie:
 *
 * templates/pebble/file
 * templates/freemarker/file
 * ...
 */
object PebbleRenderer {
    val basePath = "templates"
    val engine = PebbleEngine.Builder().build()

    fun render (template: String, context: Map<String, *>): String {
        val compiledTemplate = engine.getTemplate("$basePath/$template")
        val bundlePath = template.substring(0, template.lastIndexOf('.')).replace('/', '.')
        val bundle = ResourceBundle.getBundle("${basePath}.$bundlePath")

        val texts = bundle.keys.toList()
            .map { it to bundle.getObject(it) }
            .filter { it.second is String }
            .toMap()

        val writer = StringWriter()
        compiledTemplate.evaluate(writer, context + texts)
        return writer.toString()
    }
}
