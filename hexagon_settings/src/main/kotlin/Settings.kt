package com.hexagonkt.settings

import com.hexagonkt.logging.Logger
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize

class Settings(private val sources: List<SettingsSource> = emptyList()) {

    constructor(vararg sources: SettingsSource) :
        this(sources.toList())

    private val log: Logger by lazy { Logger(this::class) }

    val parameters: Map<*, *> by lazy {
        sources
            .map {
                it.load().also { s ->
                    if (s.isEmpty()) {
                        log.info { "No settings found for $it" }
                    }
                    else {
                        val serialize =
                            if (SerializationManager.defaultFormat == null) s.toString()
                            else s.serialize().prependIndent(" ".repeat(4))
                        log.info { "Settings loaded from $it:\n\n$serialize" }
                    }
                }
            }
            .let {
                if (it.isEmpty()) emptyMap<Any, Any>()
                else it.reduce { a, b -> a + b }
            }
    }
}
