package com.hexagonkt.settings

object SettingsManager {

    const val SETTINGS_FILE = "application"
    const val ENVIRONMENT_PREFIX = "application_"

    val defaultSources: List<SettingsSource> = listOf(
        UrlSource("classpath:$SETTINGS_FILE.json"),
        UrlSource("classpath:$SETTINGS_FILE.yml"),
        EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
        SystemPropertiesSource(SETTINGS_FILE),
        UrlSource("file:$SETTINGS_FILE.json"),
        UrlSource("file:$SETTINGS_FILE.yml"),
        UrlSource("classpath:${SETTINGS_FILE}_test.json"),
        UrlSource("classpath:${SETTINGS_FILE}_test.yml")
    )

    var settings: Settings<*> = Settings(Map::class, defaultSources)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> instance(): T =
        settings.instance as? T ?: error("")
}
