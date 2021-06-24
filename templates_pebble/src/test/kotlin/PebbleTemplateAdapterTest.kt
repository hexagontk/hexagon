package com.hexagonkt.templates.pebble

import com.hexagonkt.templates.TemplateAdapterTest

internal class PebbleTemplateAdapterTest :
    TemplateAdapterTest("templates/test.pebble.html", { PebbleAdapter })
