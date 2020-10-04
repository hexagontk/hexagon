package com.hexagonkt.settings

import com.hexagonkt.serialization.parse
import java.net.URL

class UrlSource(val url: URL) : SettingsSource {

    constructor(url: String) : this(URL(url))

    override fun toString(): String = "URL with path: $url"

    override fun load(): Map<String, *> =
        LinkedHashMap(
            try {
                // TODO Fail if resource exists but format is not loaded
                url
                    .parse<Map<String, *>>()
                    .mapKeys { e -> e.key }
            }
            catch (e: Exception) {
                emptyMap<String, Any>()
            }
        )
}
