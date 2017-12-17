package com.hexagonkt.templates.kotlinx

import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML

import java.lang.ClassLoader.getSystemResourceAsStream as resourceAsStream

/**
 * TODO Support different engines by subdir. Ie:
 * TODO Add code to test templates (check unresolved variables in bundles, multilanguage, etc.)
 *
 * templates/pebble/file
 * templates/freemarker/file
 * ...
 */
object KotlinxEngine {
    fun page(callback: TagConsumer<String>.() -> String): String = """
        <!DOCTYPE html>
        ${createHTML().callback()}
        """.trimMargin()
}
