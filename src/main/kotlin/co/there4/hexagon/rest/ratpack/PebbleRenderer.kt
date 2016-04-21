package co.there4.hexagon.rest.ratpack

import com.mitchellbosecke.pebble.PebbleEngine
import java.lang.ClassLoader.getSystemResourceAsStream
import java.io.StringWriter
import java.util.*

/**
 * TODO Support different engines by subdir. Ie:
 * TODO Extract templates to different package (useful outside REST module)
 *
 * templates/pebble/file
 * templates/freemarker/file
 * ...
 */
object PebbleRenderer {
    val basePath = "templates"
    val engine = PebbleEngine.Builder().build()
    val global = loadProps ("$basePath/global.properties")

    private fun loadProps (path: String): Map<String, *> {
        val p = Properties()
        val resourceAsStream = getSystemResourceAsStream(path)
        if (resourceAsStream != null)
            p.load (resourceAsStream)
        return p.mapKeys { it.key.toString() }
    }

    fun render (template: String, locale: Locale, context: Map<String, *>): String {
        fun loadBundle (path: String): Map<String, *> {
            try {
                val bundle = ResourceBundle.getBundle("${basePath}.$path", locale)
                return bundle.keys.toList()
                    .map { it to bundle.getObject(it) }
                    .filter { it.first is String }
                    .filter { it.second is String }
                    .toMap ()
            }
            catch (e: Exception) {
                return mapOf<String, Any> ()
            }
        }

        val compiledTemplate = engine.getTemplate("$basePath/$template")
        val bundlePath = template.substring(0, template.lastIndexOf('.')).replace('/', '.')

        val texts = loadBundle (bundlePath)
        val common = loadBundle ("common")

        val writer = StringWriter()
        compiledTemplate.evaluate(writer, global + common + texts + context, locale)
        return writer.toString()
    }
}
