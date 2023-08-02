package com.hexagonkt.templates.rocker

import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest

internal class RockerTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.rocker.html"), RockerAdapter())
