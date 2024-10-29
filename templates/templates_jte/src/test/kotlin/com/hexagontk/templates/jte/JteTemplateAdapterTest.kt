package com.hexagontk.templates.jte

import com.hexagontk.core.media.TEXT_HTML
import com.hexagontk.core.urlOf
import com.hexagontk.templates.test.TemplateAdapterTest
import org.junit.jupiter.api.condition.DisabledInNativeImage

@DisabledInNativeImage
internal class JteTemplateAdapterTest :
    TemplateAdapterTest(urlOf("classpath:templates/test.jte"), Jte(TEXT_HTML))
