package com.hexagontk.templates.jte

import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest

internal class JteTemplateAdapterPrecompiledTest :
    TemplateAdapterTest(urlOf("classpath:test.jte"), Jte(TEXT_HTML, precompiled = true))
