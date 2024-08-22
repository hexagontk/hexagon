package com.hexagontk.templates.jte

import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest

internal class JteTemplateAdapterPrecompiledBaseTest :
    TemplateAdapterTest(
        urlOf("classpath:test.jte"),
        JteAdapter(TEXT_HTML, urlOf("classpath:/"), precompiled = true)
    )
