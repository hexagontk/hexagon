package com.hexagonkt.settings

import com.hexagonkt.helpers.Resource
import com.hexagonkt.serialization.parse

class ResourceSource(val resource: Resource) : SettingsSource {

   override fun toString(): String = "Resource with path: ${resource.path}"

   override fun load(): Map<String, *> =
        resource.url().let {
            if (it == null) linkedMapOf<String, Any>()
            else LinkedHashMap(it.parse().mapKeys { e -> e.key.toString() })
        }
}
