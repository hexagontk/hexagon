package com.hexagonkt.templates.rocker

import com.fizzed.rocker.Rocker
import com.fizzed.rocker.runtime.RockerRuntime
import com.fizzed.rocker.runtime.StringBuilderOutput
import com.hexagonkt.templates.TemplatePort
import java.net.URL
import java.util.*

class RockerAdapter(reloading: Boolean = false) : TemplatePort {

    init {
        RockerRuntime.getInstance().isReloading = reloading
    }

    override fun render(url: URL, context: Map<String, *>, locale: Locale): String =
        Rocker.template(url.file)
            .bind("context", context)
            .render(StringBuilderOutput.FACTORY)
            .toString()
}
