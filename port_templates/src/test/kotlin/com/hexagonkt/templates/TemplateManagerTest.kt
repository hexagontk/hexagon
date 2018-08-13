package com.hexagonkt.templates

import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.testng.annotations.Test
import java.util.*

@Test class TemplateManagerTest {
    private object VoidTemplateAdapter : TemplatePort {
        override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
            return context.serialize()
        }
    }

    fun `Template with unparseable properties is rendered`() {
        val locale = Locale.getDefault()
        val context = mapOf("a" to "b")
        val resource = "test.pebble.html"
        val render = TemplateManager.render(VoidTemplateAdapter, resource, locale, context)
        val contextMap = render.parse()

        assert(contextMap["a"] == "b")
    }
}
