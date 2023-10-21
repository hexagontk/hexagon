package com.hexagonkt.templates.jte

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest

internal class JteTemplateAdapterPrecompiledTest :
    TemplateAdapterTest(urlOf("classpath:test.jte"), JteAdapter(TEXT_HTML, precompiled = true))
