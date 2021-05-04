package com.hexagonkt.templates

import com.hexagonkt.injection.InjectionManager
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.Reader
import java.io.StringReader
import java.util.Locale

@TestInstance(PER_CLASS)
abstract class TemplateEngineTest(private val adapter: () -> TemplatePort) {

    private object VoidTemplateAdapter : TemplatePort {
        override fun render(
            resource: String,
            locale: Locale,
            context: Map<String, *>,
            settings: TemplateEngineSettings
        ): String {
            return context.serialize()
        }
    }

    init {
        InjectionManager.bind(TemplatePort::class, adapter)
    }

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
    }

    @Test fun `Create TemplateEngine`() {
        val adapter = adapter()

        // templateEngineCreation
        // Adapter injected
        TemplateEngine()

        // Adapter provided explicitly
        TemplateEngine(adapter)
        // templateEngineCreation

        // templateEngineSettingsCreation
        val loader: (resource: String) -> Reader? = { StringReader("<html>...</html>") }
        TemplateEngine(
            adapter,
            TemplateEngineSettings(
                loader = loader, // Loader for templates
                basePath = "templates", // Appended to resource name, i. e.  "templates/resource"
                loadContext = true, // Enables context loading
            )
        )
        // templateEngineSettingsCreation
    }

    @Test fun `A static template is rendered properly`() {
        val engine = TemplateEngine(
            adapter(),
            TemplateEngineSettings(
                loader = staticTemplateLoader("resource", "CONTENT")
            )
        )

        // templateEngineUsage
        val context = mapOf("key1" to "value1", "key2" to "value2")
        val locale = Locale.getDefault()
        val rendered = engine.render("resource", locale, context)
        // templateEngineUsage

        assert(rendered == "CONTENT")
    }

    @Test fun `A static template with a basePath is rendered properly `() {
        val engine = TemplateEngine(
            adapter(),
            TemplateEngineSettings(
                loader = staticTemplateLoader("basePath/resource", "CONTENT"),
                basePath = "basePath"
            )
        )

        val context = emptyMap<String, Any>()
        val locale = Locale.getDefault()
        val rendered = engine.render("resource", locale, context)

        assert(rendered == "CONTENT")
    }

    @Test fun `Template with unparseable properties is rendered`() {
        val locale = Locale.getDefault()
        val context = mapOf("a" to "b")
        val resource = "test.pebble.html"
        val engine = TemplateEngine(
            VoidTemplateAdapter,
            TemplateEngineSettings(basePath = "templates", loadContext = true)
        )

        val render = engine.render(resource, locale, context)
        val contextMap = render.parse<Map<*, *>>()

        assert(contextMap["a"] == "b")
    }

    @Test fun `Invalid resource path will return empty map`() {
        val locale = Locale.getDefault()
        val resource = "invalid.html"
        val context = emptyMap<String, Any>()
        val engine = TemplateEngine(VoidTemplateAdapter)

        val render = engine.render(resource, locale, context)
        val contextMap = render.parse<Map<*, *>>()

        assert(contextMap.size == 2)
        assert(contextMap.containsKey("_template_"))
        assert(contextMap.containsKey("_now_"))
    }

    private fun staticTemplateLoader(resource: String, content: String) =
        { requestedResource: String ->
            if (requestedResource == resource) {
                StringReader(content)
            }
            else {
                null
            }
        }
}
