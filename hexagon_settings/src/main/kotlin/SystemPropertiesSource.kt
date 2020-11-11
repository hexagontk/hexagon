package com.hexagonkt.settings

class SystemPropertiesSource(private val prefix: String) : SettingsSource {

    override fun toString(): String = "System Properties starting with: $prefix"

    override fun load(): Map<String, *> =
        System.getProperties()
            .mapKeys { it.key.toString() }
            .filter { it.key.startsWith(prefix) }
            .map { it.key.removePrefix(prefix) to it.value }
            .toMap()
}
