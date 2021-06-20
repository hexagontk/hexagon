package com.hexagonkt.settings

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.Yaml
import com.hexagonkt.serialization.toObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HexagonSettingsSamplesTest {

    @Test fun settingsUsage() {

        SerializationManager.formats = linkedSetOf(Json, Yaml)
        SerializationManager.mapper = JacksonMapper

        // settingsUsage
        data class Configuration(
            val stringProperty: String,
            val integerProperty: Int,
            val booleanProperty: Boolean,
        )

        SettingsManager.settings = Settings(
            ObjectSource(
                "stringProperty" to "str",
                "integerProperty" to 101,
                "booleanProperty" to true
            )
        )

        val configuration = SettingsManager.settings.parameters.toObject<Configuration>()
        assert(configuration.stringProperty == "str")
        assert(configuration.integerProperty == 101)
        assert(configuration.booleanProperty)
        // settingsUsage
    }

    @Test
    @Suppress("RedundantExplicitType") // Ignoring for code used as an example
    fun dataClassesAlternative() {

        // settingsDataClasses
        data class Configuration(
            val port: Int = System.getenv("NOT_FOUND_PORT")?.toInt() ?: 1415,
            val url: String = System.getProperty("testUrl") ?: "http://example.org",
        )

        val productionConfiguration: Configuration = Configuration()

        assertEquals(1415, productionConfiguration.port)
        assertEquals("http://example.org", productionConfiguration.url)

        // For tests, values can be changed using environment variables or system properties
        System.setProperty("testUrl", "http://test.example.org")
        val testConfiguration: Configuration = Configuration()

        assertEquals(1415, testConfiguration.port)
        assertEquals("http://test.example.org", testConfiguration.url)
        // settingsDataClasses
    }
}
