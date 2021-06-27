package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplateAdapterTest

internal class FreeMarkerTemplateAdapterTest :
    TemplateAdapterTest("templates/test.freemarker.html", { FreeMarkerAdapter })
