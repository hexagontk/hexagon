package com.hexagonkt.settings

import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.helpers.resource
import com.hexagonkt.serialization.YamlFormat
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize

private object Log : CachedLogger(SettingsManager::class)

fun loadResource(resName: String): Map<String, *> = logSettings(
    "'$resName' resource",
    resource(resName).let {
        if (it == null) linkedMapOf<String, Any>()
        else LinkedHashMap(it.parse().mapKeys { e -> e.key.toString() })
    }
)

fun loadEnvironmentVariables (vararg prefixes: String): Map<String, *> = logSettings(
    "'${prefixes.joinToString(", ") { it + "*" }}' environment variables",
    emptyMap<String, Any>()
)

fun loadSystemProperties (vararg prefixes: String): Map<String, *> = logSettings(
    "'${prefixes.joinToString(", ") { it + "*" }}' system properties",
    emptyMap<String, Any>()
)

fun loadFile (name: String): Map<String, *> = logSettings(
    "'$name' file",
    emptyMap<String, Any>()
)

fun loadCommandLineArguments (vararg args: String): Map<String, *> = logSettings(
    "command line arguments",
    args
        .map { it.removePrefix("--") }
        .filter { !it.startsWith("=") }
        .map { it.split("=") }
        .filter { it.size in 1..2 }
        .map { if (it.size == 1) it[0] to true else it[0] to it[1] }
        .toMap()
)

private fun logSettings(resName: String, settings: Map<String, *>): Map<String, *> =
    settings.also {
        if (it.isEmpty()) {
            Log.info("No settings found for $resName")
        }
        else {
            val serialize = it.serialize(YamlFormat).prependIndent(" ".repeat(4))
            Log.info("Settings loaded from $resName:\n\n$serialize")
        }
    }
