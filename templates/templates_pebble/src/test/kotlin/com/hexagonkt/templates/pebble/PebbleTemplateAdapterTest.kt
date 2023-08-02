package com.hexagonkt.templates.pebble

import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest

internal class PebbleTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.pebble.html"), PebbleAdapter())
