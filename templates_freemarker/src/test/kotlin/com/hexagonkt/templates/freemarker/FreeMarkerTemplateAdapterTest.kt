package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.test.TemplateAdapterTest
import java.net.URL

internal class FreeMarkerTemplateAdapterTest :
    TemplateAdapterTest(URL("classpath:templates/test.freemarker.html"), FreeMarkerAdapter)
