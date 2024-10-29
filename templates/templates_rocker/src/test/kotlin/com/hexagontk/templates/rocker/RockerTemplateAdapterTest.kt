package com.hexagontk.templates.rocker

import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest

internal class RockerTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.rocker.html"), Rocker())
