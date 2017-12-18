package com.hexagonkt.templates

import org.testng.annotations.Test
import java.util.*

@Test(enabled = false) class TemplateManagerTest {
    private object VoidTemplateAdapter : TemplatePort {
        override fun render(resource: String, locale: Locale, context: Map<String, *>): String {
            return resource
        }
    }

    fun `Template with unparseable properties is rendered`() {
        val locale = Locale.getDefault()
        TemplateManager.render(VoidTemplateAdapter, "test.pebble.html", locale, mapOf("a" to "b"))
    }
}
