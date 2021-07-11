package com.hexagonkt.settings

import java.io.IOException
import com.hexagonkt.serialization.parse
import java.net.URL
import com.hexagonkt.logging.Logger

class UrlSource(val url: URL) : SettingsSource {

    private val logger: Logger = Logger(this::class)

    constructor(url: String) : this(URL(url))

    override fun toString(): String = "URL with path: $url"

    override fun load(): Map<String, *> =
        try {
            url.parse()
        }
        catch (e: IOException) {
            logger.warn(e) { "Error loading: $url" }
            emptyMap<String, Any>()
        }
}
