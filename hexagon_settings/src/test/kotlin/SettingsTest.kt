package com.hexagonkt.settings

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.Yaml
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File

@TestInstance(PER_CLASS)
internal class SettingsTest {

    @BeforeAll fun initialize() {
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `Load environment variables add settings with provided prefixes`() {
        assert(EnvironmentVariablesSource("PATH").load().size == 1)
    }

    @Test fun `Load system properties add variables with provided prefixes`() {
        System.setProperty("systemPrefixTest", "testing")
        System.setProperty("systemPrefixBenchmark", "benchmarking")

        assert(SystemPropertiesSource("systemPrefix").load().size == 2)
    }

    @Test fun `Load file add variables contained in that file`() {
        SerializationManager.formats = linkedSetOf(Json, Yaml)
        assert(UrlSource("file:invalid").load().isEmpty())
        val file = "src/test/resources/development.yml"
        val fileName = if (File(file).exists()) file else "hexagon_core/$file"
        assert(UrlSource("file:$fileName").load().size == 2)
    }

    @Test fun `Settings are loaded from objects`() {
        data class SampleSettings(val a: String, val c: Int)
        assert(ObjectSource("a" to "b", "c" to 0).settings == mapOf("a" to "b", "c" to 0))
        assert(ObjectSource(mapOf("a" to "z", "c" to 1)).settings == mapOf("a" to "z", "c" to 1))
        assert(ObjectSource(SampleSettings("x", 9)).settings == mapOf("a" to "x", "c" to 9))
    }

    @Test fun `ObjectSource settings properties are correct`() {
        val objectSource = ObjectSource("a" to "b", "c" to 0)
        assert(objectSource.load() == mapOf("a" to "b", "c" to 0))
        assert(objectSource.toString() == "Object Settings")
    }

    @Test fun `Load command line arguments adds correct settings `() {
        val cases = mapOf(
            arrayOf("a", "=X") to mapOf ("a" to true),
            arrayOf("a", "x=y=z") to mapOf ("a" to true),

            arrayOf("a") to mapOf ("a" to true),
            arrayOf("a", "b") to mapOf ("a" to true, "b" to true),

            arrayOf("--a") to mapOf ("a" to true),
            arrayOf("--a", "--b") to mapOf ("a" to true, "b" to true),

            arrayOf("a=1") to mapOf ("a" to "1"),
            arrayOf("a=1", "b=2") to mapOf ("a" to "1", "b" to "2"),

            arrayOf("--a=1") to mapOf ("a" to "1"),
            arrayOf("--a=1", "--b=2") to mapOf ("a" to "1", "b" to "2")
        )

        cases.forEach {
            val commandLineArgumentsSource = CommandLineArgumentsSource(it.key)
            assert(commandLineArgumentsSource.load() == it.value)
            assert(commandLineArgumentsSource.toString() == "Command Line Arguments")
        }
    }
}
