package com.hexagonkt.settings

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.SerializationManager.formats
import com.hexagonkt.serialization.Yaml
import com.hexagonkt.settings.SettingsManager.ENVIRONMENT_PREFIX
import com.hexagonkt.settings.SettingsManager.SETTINGS_FILE
import com.hexagonkt.settings.SettingsManager.defaultSources
import com.hexagonkt.settings.SettingsManager.settings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Check `gradle.build` to see the related files creation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SettingsManagerTest {

    data class Configuration(
        val foo: String = "bar",
        val property: String = "",
        val added: Boolean = true,
        val integer: Int = 0,
        val banner: String = "",
        val bindPort: Int = 0,
        val bindAddress: String = "",
        val intProperty: Int = 0,
        val global: String = "default",
        val parent: Map<String, String> = emptyMap(),
        val key: String = "",
        val param: String = "",
    )

    @BeforeEach fun resetSettingSources() {
        SerializationManager.mapper = JacksonMapper
        formats = linkedSetOf(Json, Yaml)
        settings = Settings(Map::class, defaultSources)
    }

    @Test fun `Loading existing resource with not loaded content format fails`() {

        formats = linkedSetOf(Json)
        assertFailsWith<IllegalStateException> { UrlSource("classpath:development.yml").load() }

        formats = linkedSetOf(Json, Yaml)
        assertEquals("bar", UrlSource("classpath:development.yml").load()["foo"])
    }

    @Test fun `Settings works as expected`() {

        val settings = Settings(Configuration::class, defaultSources).instance

        assert(settings.property == "value")
        assert(settings.intProperty == 42)
        assert(settings.foo == "bar")
    }

    @Test fun `Get configuration properties with empty properties load properly`() {

        val settings = Settings(Configuration::class).instance
        assert(settings.global == "default")
    }

    @Test fun `Get configuration properties with defaults`() {

        val settings = Settings(Configuration::class, defaultSources).instance

        assert(settings.integer == 0)
        assert(settings.foo == "bar")
    }

    @Test fun `Get JSON properties`() {

        val settings = Settings(Configuration::class,
            UrlSource("classpath:$SETTINGS_FILE.yml"),
            UrlSource("classpath:development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS_FILE),
            UrlSource("file:$SETTINGS_FILE.yml"),
            UrlSource("classpath:${SETTINGS_FILE}_test.yml"),
            UrlSource("classpath:integration.json"),
        ).instance

        assert(settings.property == "final property")
        assert(settings.intProperty == 42)
        assert(settings.foo == "final")
        assert(settings.parent["key"] == "val")
        assert(settings.added)
        assert(settings.integer == 1)
    }

    @Test fun `Get configuration properties`() {

        val settings = Settings(Configuration::class,
            UrlSource("classpath:$SETTINGS_FILE.yml"),
            UrlSource("classpath:development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS_FILE),
            UrlSource("file:$SETTINGS_FILE.yml"),
            UrlSource("classpath:${SETTINGS_FILE}_test.yml"),
        ).instance

        assert(settings.property == "value")
        assert(settings.intProperty == 42)
        assert(settings.foo == "bar")
        assert(settings.parent["key"] == "val")
    }

    @Test fun `Change settings manager settings works correctly`() {
        settings = Settings(Configuration::class,
            ObjectSource(
                "foo" to "str",
                "integer" to 101,
                "added" to true
            )
        )

        val localSettings = SettingsManager.instance<Configuration>()

        assert(localSettings.foo == "str")
        assert(localSettings.integer == 101)
        assert(localSettings.added)
    }

    @Test fun `Set default settings add command line arguments`() {

        val settings = Settings(Configuration::class,
            UrlSource("classpath:$SETTINGS_FILE.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS_FILE),
            UrlSource("file:$SETTINGS_FILE.yml"),
            UrlSource("classpath:${SETTINGS_FILE}_test.yml"),
            CommandLineArgumentsSource(listOf("key=val", "param=data")),
        ).instance

        assert(settings.key == "val")
        assert(settings.param == "data")
        assert(settings.property == "value")
    }
}
