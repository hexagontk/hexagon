package com.hexagonkt.settings

class CommandLineArgumentsSource(val args: List<String>) : SettingsSource {

    override fun toString(): String = "Command Line Arguments"

    override fun load(): Map<String, *> =
        args
            .map { it.removePrefix("--") }
            .filter { !it.startsWith("=") }
            .map { it.split("=") }
            .filter { it.size <= 2 }
            .map { if (it.size == 1) it[0] to true else it[0] to it[1] }
            .toMap()
}
