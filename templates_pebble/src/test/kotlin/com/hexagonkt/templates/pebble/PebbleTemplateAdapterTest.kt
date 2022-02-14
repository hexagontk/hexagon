package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.test.TemplateAdapterTest
import java.net.URL

internal class PebbleTemplateAdapterTest :
    TemplateAdapterTest(URL("classpath:templates/test.pebble.html"), PebbleAdapter)
