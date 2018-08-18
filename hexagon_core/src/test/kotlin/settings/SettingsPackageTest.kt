package com.hexagonkt.settings

import org.testng.annotations.Test
import java.io.File

@Test class SettingsPackageTest {
    fun `Load environment variables add settings with provided prefixes`() {
        assert(loadEnvironmentVariables("PATH", "INVALID").size == 1)
        assert(loadEnvironmentVariables("PATH", "USER").size == 2)
    }

    fun `Load system properties add variables with provided prefixes`() {
        System.setProperty("systemPrefixTest", "testing")
        System.setProperty("systemPrefixBenchmark", "benchmarking")

        assert(loadSystemProperties("systemPrefix", "invalid").size == 2)
    }

    fun `Load file add variables contained in that file`() {
        assert(loadFile("invalid").isEmpty())
        val file = "src/test/resources/development.yaml"
        val fileName = if (File(file).exists()) file else "hexagon_core/$file"
        assert(loadFile(fileName).size == 2)
    }

    fun `Load command line arguments adds correct settings `() {
        val cases = mapOf(
            listOf("a", "=X") to mapOf ("a" to true),
            listOf("a", "x=y=z") to mapOf ("a" to true),

            listOf("a") to mapOf ("a" to true),
            listOf("a", "b") to mapOf ("a" to true, "b" to true),

            listOf("--a") to mapOf ("a" to true),
            listOf("--a", "--b") to mapOf ("a" to true, "b" to true),

            listOf("a=1") to mapOf ("a" to "1"),
            listOf("a=1", "b=2") to mapOf ("a" to "1", "b" to "2"),

            listOf("--a=1") to mapOf ("a" to "1"),
            listOf("--a=1", "--b=2") to mapOf ("a" to "1", "b" to "2")
        )

        cases.forEach {
            assert(loadCommandLineArguments(*it.key.toTypedArray()) == it.value)
        }
    }
}
