package com.hexagonkt.settings

import com.hexagonkt.serialization.parse
import java.net.URL

class ResourceSource(val resource: URL) : SettingsSource {

    constructor(resource: String) : this(URL(resource))

    override fun toString(): String = "Resource with path: ${resource.path}"

    @Suppress("RemoveExplicitTypeArguments") // Compile error filed as warning inside IntelliJ
    override fun load(): Map<String, *> =
        LinkedHashMap(
            try {
                resource
                    .parse<Map<String, *>>()
                    .mapKeys { e -> e.key }
            }
            catch (e: Exception) {
                emptyMap<String, Any>()
            }
        )
}
