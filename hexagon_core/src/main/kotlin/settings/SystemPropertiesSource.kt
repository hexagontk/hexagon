package com.hexagonkt.settings

class SystemPropertiesSource(val prefixes: List<String>) : SettingsSource {

    constructor(vararg prefixes: String) : this(prefixes.toList())

    override fun toString(): String = "System Properties starting with: ${prefixes.joinToString(", ")}"

    override fun load(): Map<String, *> =
        System.getProperties()
            .mapKeys { it.key.toString() }
            .filter { property ->
                prefixes.filter { property.key.startsWith(it) }.any()
            }
            .map { it.key to it.value }
            .toMap()
}
