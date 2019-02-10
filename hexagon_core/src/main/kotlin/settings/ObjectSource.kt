package com.hexagonkt.settings

import com.hexagonkt.serialization.convertToMap

class ObjectSource(val settings: Map<String, *>) : SettingsSource {

    constructor (vararg pairs: Pair<String, *>) : this(pairs.toMap())

    constructor (instance: Any) : this(instance.convertToMap().mapKeys { it.key.toString() })

    override fun toString(): String = "Object Settings"

    override fun load(): Map<String, *> = settings
}
