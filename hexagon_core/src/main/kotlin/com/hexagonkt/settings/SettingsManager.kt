package com.hexagonkt.settings

import com.hexagonkt.helpers.*
import com.hexagonkt.serialization.parse

object SettingsManager : Loggable {
    // TODO Handle environment like a setting (if it is loaded, then look for special resources
    var environment: String? = null
        private set

    var settings: Map<String, *> = loadResource("service.yaml") + loadResource("service_test.yaml")
        private set

    init {
        environment = settings["ENVIRONMENT"] as? String
        environment = "DEVELOPMENT"
        // Examples
//        environment += loadProps("")
//        environment += "foo" to loadProps("")
        if (environment != null) settings += loadResource("${environment?.toLowerCase()}.yaml")
    }

//    private fun loadEnvironmentVariables (): Map<String, *> = TODO()
//    private fun loadEnvironmentVariables (prefix: String): Map<String, *> = TODO()
//    private fun loadCommandLineArguments (vararg args: String): Map<String, *> = TODO()
//    private fun loadSystemProperties (vararg args: String): Map<String, *> = TODO()
//    private fun loadFiles (vararg args: String): Map<String, *> = TODO()

    @Suppress("UNCHECKED_CAST")
    private fun loadResource(resName: String): Map<String, *> =
        resource(resName).let {
            if (it == null) {
                info("No environment settings found '$resName'")
                mapOf<String, Any>()
            }
            else {
                val props: Map<String, *> = it.parse().mapKeys { e -> e.key.toString() }
                val separator = eol + " ".repeat(4)
                info("Settings loaded from '$resName':" +
                    props
                        .map { it.key + " : " + it.value }
                        .joinToString(separator, separator, eol)
                )
                props
            }
        }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")
}
