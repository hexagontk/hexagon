package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplateAdapterTest
import java.net.URL

internal class PebbleTemplateAdapterTest :
    TemplateAdapterTest(URL("classpath:templates/test.pebble.html"), PebbleAdapter)
