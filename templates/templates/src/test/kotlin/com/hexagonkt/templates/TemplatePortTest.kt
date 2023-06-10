package com.hexagonkt.templates

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TemplatePortTest {

    @Test fun `Templates from literals are handled properly`() {
        val adapter = SampleTemplateAdapter("prefix ")
        assertEquals("prefix template code", adapter.render("template code", mapOf("var" to "val")))
    }
}
