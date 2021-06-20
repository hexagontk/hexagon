package com.hexagonkt.templates.freemarker

import com.hexagonkt.templates.TemplateEngineTest

internal class FreeMarkerTemplateEngineTest :
    TemplateEngineTest("templates/test.freemarker.html", { FreeMarkerAdapter })
