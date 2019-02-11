package com.hexagonkt.settings

class EnvironmentVariablesSource(private val prefix: String) : SettingsSource {

    override fun toString(): String = "Environment Variables starting with: $prefix"

    override fun load(): Map<String, *> =
        System.getenv()
            .filter { it.key.startsWith(prefix) }
            .map { it.key.removePrefix(prefix) to it.value }
            .toMap()
}
