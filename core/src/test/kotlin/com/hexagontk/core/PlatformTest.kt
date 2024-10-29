package com.hexagontk.core

import org.junit.jupiter.api.Test
import kotlin.IllegalArgumentException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URI
import java.net.URL
import kotlin.test.*

internal class PlatformTest {

    @Test fun `Console availability works ok`() {
        // From tests, you never have a TTY, positive case hard to test
        assertFalse(Platform.isConsole)
        assertEquals(
            "Program doesn't have a console (I/O may be redirected)",
            assertFailsWith<IllegalStateException> { Platform.console }.message
        )
    }

    @Test fun `System settings are loaded properly properly`() {
        mapOf("s1" to "v1", "s2" to "v2").forEach { (k, v) -> System.setProperty(k, v) }

        Platform.loadSystemSettings(mapOf("s1" to "x1", "s2" to "x2"))
        assertEquals("x1", System.getProperty("s1"))
        assertEquals("x2", System.getProperty("s2"))

        Platform.loadSystemSettings(mapOf("s1" to "v1", "s2" to "v2", "s3" to "x3"))
        assertEquals("v1", System.getProperty("s1"))
        assertEquals("v2", System.getProperty("s2"))
        assertEquals("x3", System.getProperty("s3"))

        val e = assertFailsWith<IllegalStateException> {
            Platform.loadSystemSettings(mapOf("1" to "v"))
        }
        assertEquals("Property name must match [_A-Za-z]+[_A-Za-z0-9]* (1)", e.message)
    }

    @Test fun `OS kind is fetched properly`() {
        val os = Platform.os
        assert(os.isNotBlank())
        assert(Platform.osKind in OsKind.entries)

        System.clearProperty("os.name")
        assertEquals(
            "OS property ('os.name') not found",
            assertFailsWith<IllegalStateException> { Platform.os() }.message
        )

        System.setProperty("os.name", "MS-DOS")
        assertEquals(
            "Unsupported OS: MS-DOS",
            assertFailsWith<IllegalStateException> { Platform.osKind() }.message
        )

        checkOsKind("Windows", OsKind.WINDOWS)
        checkOsKind("windows", OsKind.WINDOWS)
        checkOsKind("win", OsKind.WINDOWS)
        checkOsKind("Win", OsKind.WINDOWS)

        checkOsKind("macOS", OsKind.MACOS)
        checkOsKind("macos", OsKind.MACOS)
        checkOsKind("mac", OsKind.MACOS)
        checkOsKind("MAC", OsKind.MACOS)

        checkOsKind("Linux", OsKind.LINUX)
        checkOsKind("linux", OsKind.LINUX)
        checkOsKind("Debian Linux", OsKind.LINUX)
        checkOsKind("debian linux", OsKind.LINUX)

        checkOsKind("aix", OsKind.UNIX)
        checkOsKind("AIX", OsKind.UNIX)
        checkOsKind("IBM AIX", OsKind.UNIX)
        checkOsKind("BSD Unix", OsKind.UNIX)
        checkOsKind("bsd unix", OsKind.UNIX)

        System.setProperty("os.name", os)
    }

    @Test fun `'systemFlag' fails with a blank setting name`() {
        assertFailsWith<IllegalArgumentException> { Platform.systemFlag("") }
        assertFailsWith<IllegalArgumentException> { Platform.systemFlag(" ") }
        assertFailsWith<IllegalArgumentException> { Platform.systemSettingOrNull<String>("") }
        assertFailsWith<IllegalArgumentException> { Platform.systemSettingOrNull<String>(" ") }
        assertFailsWith<IllegalArgumentException> { Platform.systemSettingOrNull<String>("1") }
        assertFailsWith<IllegalArgumentException> { Platform.systemSettingOrNull<String>("#") }
    }

    @Test fun `'systemFlag' returns true on defined boolean parameter`() {
        assertFalse { Platform.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "false")
        assertFalse { Platform.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "T")
        assertFalse { Platform.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "True")
        assertFalse { Platform.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "true")
        assertTrue { Platform.systemFlag("TEST_FLAG") }
        System.clearProperty("TEST_FLAG")
    }

    @Test fun `'hostname' and 'ip' contains valid values` () {
        val ipv6Segment = "[0-9a-zA-Z]{0,4}"
        val ipv6Regex = Regex("$ipv6Segment(:$ipv6Segment)*(%\\d+)?")
        val ipv4Regex = Regex("\\d{1,3}(\\.\\d{1,3}){3}")

        assert(Platform.ip.matches(ipv4Regex) || Platform.ip.matches(ipv6Regex))
        assert(Platform.hostName.isNotBlank())
        assert(Inet4Address.getAllByName(Platform.hostName).isNotEmpty())
        assert(Inet4Address.getAllByName(Platform.ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(Platform.totalMemory().matches(numberRegex))
        assert(Platform.usedMemory().matches(numberRegex))
    }

    @Test fun `Locale code matches with default locale` () {
        assert(Platform.localeCode.contains(Platform.locale.country))
        assert(Platform.localeCode.contains(Platform.locale.language))
    }

    @Test fun `System settings handle parameter types correctly`() {
        System.setProperty("validBoolean", true.toString())
        System.setProperty("validInt", 123.toString())
        System.setProperty("validLong", 456L.toString())
        System.setProperty("validFloat", 0.5F.toString())
        System.setProperty("validDouble", 1.5.toString())
        System.setProperty("validInetAddress", LOOPBACK_INTERFACE.hostName)
        System.setProperty("validURL", urlOf("http://host:1234/path").toString())
        System.setProperty("validURI", URI("ws://host:1234/path").toString())
        System.setProperty("invalidBoolean", "_")
        System.setProperty("invalidInt", "_")
        System.setProperty("invalidLong", "_")
        System.setProperty("invalidFloat", "_")
        System.setProperty("invalidDouble", "_")
        System.setProperty("invalidInetAddress", "_")
        System.setProperty("invalidURL", "_")
        System.setProperty("invalidURI", "_")
        System.setProperty("string", "text")
        System.setProperty("error", "value")

        assertEquals(true, Platform.systemSetting(Boolean::class, "validBoolean"))
        assertEquals(123, Platform.systemSetting(Int::class, "validInt"))
        assertEquals(456L, Platform.systemSetting(Long::class, "validLong"))
        assertEquals(0.5F, Platform.systemSetting(Float::class, "validFloat"))
        assertEquals(1.5, Platform.systemSetting(Double::class, "validDouble"))
        assertEquals(urlOf("http://host:1234/path"), Platform.systemSetting(URL::class, "validURL"))
        assertEquals(URI("ws://host:1234/path"), Platform.systemSetting(URI::class, "validURI"))
        assertEquals(
            LOOPBACK_INTERFACE, Platform.systemSetting(InetAddress::class, "validInetAddress")
        )

        assertEquals(true, Platform.systemSetting("validBoolean"))
        assertEquals(123, Platform.systemSetting("validInt"))
        assertEquals(456L, Platform.systemSetting("validLong"))
        assertEquals(0.5F, Platform.systemSetting("validFloat"))
        assertEquals(1.5, Platform.systemSetting("validDouble"))
        assertEquals(LOOPBACK_INTERFACE, Platform.systemSetting("validInetAddress"))
        assertEquals(urlOf("http://host:1234/path"), Platform.systemSetting("validURL"))
        assertEquals(URI("ws://host:1234/path"), Platform.systemSetting("validURI"))

        assertNull(Platform.systemSettingOrNull<Int>("invalidInt"))
        assertNull(Platform.systemSettingOrNull<Long>("invalidLong"))
        assertNull(Platform.systemSettingOrNull<Float>("invalidFloat"))
        assertNull(Platform.systemSettingOrNull<Double>("invalidDouble"))
        assertNull(Platform.systemSettingOrNull<Boolean>("invalidBoolean"))
        assertNull(Platform.systemSettingOrNull<Boolean>("invalidInetAddress"))
        assertNull(Platform.systemSettingOrNull<Boolean>("invalidURL"))
        assertNull(Platform.systemSettingOrNull<Boolean>("invalidURI"))

        assertEquals("text", Platform.systemSetting("string"))

        assertEquals(true, Platform.systemSetting(Boolean::class, "validBoolean"))
        assertNull(Platform.systemSettingOrNull(Boolean::class, "invalidBoolean"))

        val type = System::class
        val e = assertFailsWith<IllegalArgumentException> {
            Platform.systemSettingOrNull<System>("error")
        }

        assertEquals("Unsupported type: ${type.qualifiedName}", e.message)
    }

    @Test fun `Default time zone is fetched correctly`() {
        assertNotNull(Platform.timeZone)
        assertNotNull(Platform.zoneId)
    }

    @Test fun `Default JVM info is fetched correctly`() {
        assert(Platform.cpuCount > 0)
    }

    @Test fun `Default charset is fetched correctly`() {
        assert(Platform.charset.isRegistered)
        assert(Platform.charset.canEncode())
    }

    @Test fun `JVM version is retrieved`() {
        assert(Platform.version.isNotBlank())
    }

    @Test fun `System setting works ok`() {
        System.setProperty("system_property", "value")

        assert(Platform.systemSetting<String>("system_property") == "value")

        assert(Platform.systemSetting<String>("PATH").isNotEmpty())
        assert(Platform.systemSetting<String>("path").isNotEmpty())
        assertNotEquals("default", Platform.systemSetting<String>("path", "default"))
        assertNull(Platform.systemSettingOrNull<String>("_not_defined_"))
        assertEquals("default", Platform.systemSetting<String>("_not_defined_", "default"))

        System.setProperty("PATH", "path override")
        assert(Platform.systemSetting<String>("PATH") != "path override")
    }

    private fun checkOsKind(osName: String, osKind: OsKind) {
        System.setProperty("os.name", osName)
        assertEquals(osKind, Platform.osKind())
    }
}
