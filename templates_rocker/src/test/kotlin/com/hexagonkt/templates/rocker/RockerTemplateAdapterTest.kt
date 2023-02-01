package com.hexagonkt.templates.rocker

import com.hexagonkt.templates.test.TemplateAdapterTest
import java.net.URL

internal class RockerTemplateAdapterTest :
    TemplateAdapterTest(URL("classpath:templates/test.rocker.html"), RockerAdapter())
