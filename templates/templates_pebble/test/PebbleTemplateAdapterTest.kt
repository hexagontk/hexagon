package com.hexagontk.templates.pebble

import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest

internal class PebbleTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.pebble.html"), Pebble())
