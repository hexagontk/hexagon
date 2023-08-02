package com.hexagonkt.templates.freemarker

import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest

internal class FreeMarkerTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.freemarker.html"), FreeMarkerAdapter())
