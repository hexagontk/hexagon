package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplateAdapterTest
import java.net.URL

internal class FreeMarkerTemplateAdapterTest :
    TemplateAdapterTest(URL("classpath:templates/test.freemarker.html"), FreeMarkerAdapter)
