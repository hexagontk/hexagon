package com.hexagonkt.settings

import org.testng.annotations.Test

@Test class SettingsPackageTest {
    fun `load environment variables add settings with provided prefixes`() {
        assert(loadEnvironmentVariables("PATH", "INVALID").size == 1)
        assert(loadEnvironmentVariables("PATH", "SHELL", "USER").size == 3)
    }

    fun `load system properties add variables with provided prefixes`() {
        System.setProperty("systemPrefixTest", "testing")
        System.setProperty("systemPrefixBenchmark", "benchmarking")

        assert(loadSystemProperties("systemPrefix", "invalid").size == 2)
    }

    fun `load file add variables contained in that file`() {
        assert(loadFile("invalid").isEmpty())
        assert(loadFile("src/test/resources/development.yaml").size == 2)
    }

    fun `load command line arguments adds correct settings `() {
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
