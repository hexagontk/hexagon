package com.hexagontk.templates.freemarker

import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest

internal class FreeMarkerTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.freemarker.html"), FreeMarker())
