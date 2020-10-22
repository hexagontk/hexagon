package com.hexagonkt.templates

import java.io.Reader

/**
 * TemplateEngineSettings are used to configure a [TemplateEngine]
 *
 * @param loader loads templates
 * @param loadContext if enabled, properties are loaded from the classpath
 * @param basePath appended to resource name, i. e. "templates/resource"
 */
data class TemplateEngineSettings(
    val loader: ((resource: String) -> Reader?)? = null,
    val loadContext: Boolean = true,
    val basePath: String? = null
)
