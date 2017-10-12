package com.hexagonkt.templates.rocker

import com.fizzed.rocker.TemplateBindException
import org.testng.annotations.Test
import java.util.*

@Test class RockerEngineTest {
    fun `render template works` () {
        val locale = Locale.getDefault()
        val template = RockerEngine.render("template.rocker.html", locale, mapOf("param" to "foo"))
        assert (template.contains("Test"))
    }

    @Test(expectedExceptions = arrayOf(TemplateBindException::class))
    fun `render template with bad parameters fails` () {
        RockerEngine.render("template.rocker.html", Locale.getDefault(), mapOf("param" to 0))
    }
}
