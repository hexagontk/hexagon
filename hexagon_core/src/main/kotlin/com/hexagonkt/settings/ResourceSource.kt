package com.hexagonkt.settings

import com.hexagonkt.helpers.Resource
import com.hexagonkt.serialization.parse

class ResourceSource(val resource: Resource) : SettingsSource {

    constructor(resource: String) : this(Resource(resource))

    override fun toString(): String = "Resource with path: ${resource.path}"

    @Suppress("RemoveExplicitTypeArguments") // Compile error filed as warning inside IntelliJ
    override fun load(): Map<String, *> =
        resource.url().let {
            if (it == null) linkedMapOf<String, Any>()
            else LinkedHashMap(it.parse<Map<String, *>>().mapKeys { e -> e.key })
        }
}
