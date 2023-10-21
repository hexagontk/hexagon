package com.hexagonkt.templates.jte

import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.core.urlOf
import com.hexagonkt.templates.test.TemplateAdapterTest
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
internal class JteTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.jte"), JteAdapter(TEXT_HTML))
