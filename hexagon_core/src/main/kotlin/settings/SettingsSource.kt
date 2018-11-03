package com.hexagonkt.settings

interface SettingsSource {
    fun load(): Map<String, *>
}
