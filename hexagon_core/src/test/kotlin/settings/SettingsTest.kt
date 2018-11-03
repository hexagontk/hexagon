package com.hexagonkt.settings

import org.testng.annotations.Test
import java.io.File

@Test class SettingsTest {
    fun `Load environment variables add settings with provided prefixes`() {
        assert(EnvironmentVariablesSource("PATH", "INVALID").load().size == 1)
        assert(EnvironmentVariablesSource("PATH", "USER").load().size == 2)
    }

    fun `Load system properties add variables with provided prefixes`() {
        System.setProperty("systemPrefixTest", "testing")
        System.setProperty("systemPrefixBenchmark", "benchmarking")

        assert(SystemPropertiesSource("systemPrefix", "invalid").load().size == 2)
    }

    fun `Load file add variables contained in that file`() {
        assert(FileSource("invalid").load().isEmpty())
        val file = "src/test/resources/development.yaml"
        val fileName = if (File(file).exists()) file else "hexagon_core/$file"
        assert(FileSource(fileName).load().size == 2)
    }

    fun `Load command line arguments adds correct settings `() {
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
            assert(CommandLineArgumentsSource(it.key).load() == it.value)
        }
    }
}
