package com.hexagonkt.settings

import com.hexagonkt.helpers.eol
import com.hexagonkt.helpers.resource
import com.hexagonkt.serialization.parse

//private fun loadEnvironmentVariables (vararg prefixes: String): Map<String, *> = TODO()
//private fun loadCommandLineArguments (vararg args: String): Map<String, *> = TODO()
//private fun loadSystemProperties (vararg prefixes: String): Map<String, *> = TODO()
//private fun loadFiles (vararg args: String): Map<String, *> = TODO()

@Suppress("UNCHECKED_CAST")
fun loadResource(resName: String): Map<String, *> =
    resource(resName).let {
        if (it == null) {
//            info("No environment settings found '$resName'")
            linkedMapOf<String, Any>()
        }
        else {
            val props: LinkedHashMap<String, *> = LinkedHashMap(
                it.parse().mapKeys { e -> e.key.toString() }
            )
            val separator = eol + " ".repeat(4)
//            info("Settings loaded from '$resName':" +
//                props
//                    .map { it.key + " : " + it.value }
//                    .joinToString(separator, separator, eol)
//            )
            props
        }
    }
