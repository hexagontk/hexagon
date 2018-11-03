package com.hexagonkt.settings

class EnvironmentVariablesSource(val prefixes: List<String>) : SettingsSource {

    override fun toString(): String = "Environment Variables starting with: ${prefixes.joinToString(", ")}"

    override fun load(): Map<String, *> =
        System.getenv()
            .filter { property -> prefixes.filter { property.key.startsWith(it) }.any() }
            .map { it.key to it.value }
            .toMap()
}
