package com.hexagonkt.settings

import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.helpers.resource
import com.hexagonkt.serialization.YamlFormat
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import java.io.File
import java.io.FileNotFoundException

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
    System.getenv()
        .filter { property ->
            prefixes.filter { property.key.startsWith(it) }.any()
        }
        .map { it.key to it.value }
        .toMap()
)

fun loadSystemProperties (vararg prefixes: String): Map<String, *> = logSettings(
    "'${prefixes.joinToString(", ") { it + "*" }}' system properties",
    System.getProperties()
        .mapKeys { it.key.toString() }
        .filter { property ->
            prefixes.filter { property.key.startsWith(it) }.any()
        }
        .map { it.key to it.value }
        .toMap()
)

fun loadFile (file: File): Map<String, *> = logSettings(
    "'${file.absolutePath}' file",
    try {
        file.parse().mapKeys { e -> e.key.toString() }
    }
    catch (e: FileNotFoundException) {
        emptyMap<String, Any>()
    }
)

fun loadFile (name: String): Map<String, *> = loadFile(File(name))

fun loadCommandLineArguments (vararg args: String): Map<String, *> = logSettings(
    "command line arguments",
    args
        .map { it.removePrefix("--") }
        .filter { !it.startsWith("=") }
        .map { it.split("=") }
        .filter { it.size <= 2 }
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
