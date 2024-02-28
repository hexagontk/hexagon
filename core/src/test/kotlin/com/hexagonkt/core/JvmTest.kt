package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.IllegalArgumentException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URI
import java.net.URL
import kotlin.test.*

internal class JvmTest {

    @Test fun `Console availability works ok`() {
        // From tests, you never have a TTY, positive case hard to test
        assertFalse(Jvm.isConsole)
        assertEquals(
            "Program doesn't have a console (I/O may be redirected)",
            assertFailsWith<IllegalStateException> { Jvm.console }.message
        )
    }

    @Test fun `System settings are loaded properly properly`() {
        mapOf("s1" to "v1", "s2" to "v2").forEach { (k, v) -> System.setProperty(k, v) }

        Jvm.loadSystemSettings(mapOf("s1" to "x1", "s2" to "x2"))
        assertEquals("x1", System.getProperty("s1"))
        assertEquals("x2", System.getProperty("s2"))

        Jvm.loadSystemSettings(mapOf("s1" to "v1", "s2" to "v2", "s3" to "x3"))
        assertEquals("v1", System.getProperty("s1"))
        assertEquals("v2", System.getProperty("s2"))
        assertEquals("x3", System.getProperty("s3"))

        val e = assertFailsWith<IllegalStateException> { Jvm.loadSystemSettings(mapOf("1" to "v")) }
        assertEquals("Property name must match [_A-Za-z]+[_A-Za-z0-9]* (1)", e.message)
    }

    @Test fun `OS kind is fetched properly`() {
        val os = Jvm.os
        assert(os.isNotBlank())
        assert(Jvm.osKind in OsKind.entries)

        System.clearProperty("os.name")
        assertEquals(
            "OS property ('os.name') not found",
            assertFailsWith<IllegalStateException> { Jvm.os() }.message
        )

        System.setProperty("os.name", "MS-DOS")
        assertEquals(
            "Unsupported OS: MS-DOS",
            assertFailsWith<IllegalStateException> { Jvm.osKind() }.message
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
        assertFailsWith<IllegalArgumentException> { Jvm.systemFlag("") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemFlag(" ") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>("") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>(" ") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>("1") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>("#") }
    }

    @Test fun `'systemFlag' returns true on defined boolean parameter`() {
        assertFalse { Jvm.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "false")
        assertFalse { Jvm.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "T")
        assertFalse { Jvm.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "True")
        assertFalse { Jvm.systemFlag("TEST_FLAG") }
        System.setProperty("TEST_FLAG", "true")
        assertTrue { Jvm.systemFlag("TEST_FLAG") }
        System.clearProperty("TEST_FLAG")
    }

    @Test fun `'hostname' and 'ip' contains valid values` () {
        val ipv6Segment = "[0-9a-zA-Z]{0,4}"
        val ipv6Regex = Regex("$ipv6Segment(:$ipv6Segment)*(%\\d+)?")
        val ipv4Regex = Regex("\\d{1,3}(\\.\\d{1,3}){3}")

        assert(Jvm.ip.matches(ipv4Regex) || Jvm.ip.matches(ipv6Regex))
        assert(Jvm.hostName.isNotBlank())
        assert(Inet4Address.getAllByName(Jvm.hostName).isNotEmpty())
        assert(Inet4Address.getAllByName(Jvm.ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(Jvm.totalMemory().matches(numberRegex))
        assert(Jvm.usedMemory().matches(numberRegex))
    }

    @Test fun `Locale code matches with default locale` () {
        assert(Jvm.localeCode.contains(Jvm.locale.country))
        assert(Jvm.localeCode.contains(Jvm.locale.language))
    }

    @Test fun `System settings handle parameter types correctly`() {
        System.setProperty("validBoolean", true.toString())
        System.setProperty("validInt", 123.toString())
        System.setProperty("validLong", 456L.toString())
        System.setProperty("validFloat", 0.5F.toString())
        System.setProperty("validDouble", 1.5.toString())
        System.setProperty("validInetAddress", LOOPBACK_INTERFACE.hostName)
        System.setProperty("validURL", urlOf("http://localhost:1234/path").toString())
        System.setProperty("validURI", URI("ws://localhost:1234/path").toString())
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

        assertEquals(true, Jvm.systemSetting(Boolean::class, "validBoolean"))
        assertEquals(123, Jvm.systemSetting(Int::class, "validInt"))
        assertEquals(456L, Jvm.systemSetting(Long::class, "validLong"))
        assertEquals(0.5F, Jvm.systemSetting(Float::class, "validFloat"))
        assertEquals(1.5, Jvm.systemSetting(Double::class, "validDouble"))
        assertEquals(LOOPBACK_INTERFACE, Jvm.systemSetting(InetAddress::class, "validInetAddress"))
        assertEquals(urlOf("http://localhost:1234/path"), Jvm.systemSetting(URL::class, "validURL"))
        assertEquals(URI("ws://localhost:1234/path"), Jvm.systemSetting(URI::class, "validURI"))

        assertEquals(true, Jvm.systemSetting("validBoolean"))
        assertEquals(123, Jvm.systemSetting("validInt"))
        assertEquals(456L, Jvm.systemSetting("validLong"))
        assertEquals(0.5F, Jvm.systemSetting("validFloat"))
        assertEquals(1.5, Jvm.systemSetting("validDouble"))
        assertEquals(LOOPBACK_INTERFACE, Jvm.systemSetting("validInetAddress"))
        assertEquals(urlOf("http://localhost:1234/path"), Jvm.systemSetting("validURL"))
        assertEquals(URI("ws://localhost:1234/path"), Jvm.systemSetting("validURI"))

        assertNull(Jvm.systemSettingOrNull<Int>("invalidInt"))
        assertNull(Jvm.systemSettingOrNull<Long>("invalidLong"))
        assertNull(Jvm.systemSettingOrNull<Float>("invalidFloat"))
        assertNull(Jvm.systemSettingOrNull<Double>("invalidDouble"))
        assertNull(Jvm.systemSettingOrNull<Boolean>("invalidBoolean"))
        assertNull(Jvm.systemSettingOrNull<Boolean>("invalidInetAddress"))
        assertNull(Jvm.systemSettingOrNull<Boolean>("invalidURL"))
        assertNull(Jvm.systemSettingOrNull<Boolean>("invalidURI"))

        assertEquals("text", Jvm.systemSetting("string"))

        assertEquals(true, Jvm.systemSetting(Boolean::class, "validBoolean"))
        assertNull(Jvm.systemSettingOrNull(Boolean::class, "invalidBoolean"))

        val type = System::class
        val e = assertFailsWith<IllegalArgumentException> {
            Jvm.systemSettingOrNull<System>("error")
        }

        assertEquals("Unsupported type: ${type.qualifiedName}", e.message)
    }

    @Test fun `Default time zone is fetched correctly`() {
        assertNotNull(Jvm.timeZone)
        assertNotNull(Jvm.zoneId)
    }

    @Test fun `Default JVM info is fetched correctly`() {
        assert(Jvm.cpuCount > 0)
    }

    @Test fun `Default charset is fetched correctly`() {
        assert(Jvm.charset.isRegistered)
        assert(Jvm.charset.canEncode())
    }

    @Test fun `JVM version is retrieved`() {
        assert(Jvm.version.isNotBlank())
    }

    @Test fun `System setting works ok`() {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting<String>("system_property") == "value")

        assert(Jvm.systemSetting<String>("PATH").isNotEmpty())
        assert(Jvm.systemSetting<String>("path").isNotEmpty())
        assertNotEquals("default", Jvm.systemSetting<String>("path", "default"))
        assertNull(Jvm.systemSettingOrNull<String>("_not_defined_"))
        assertEquals("default", Jvm.systemSetting<String>("_not_defined_", "default"))

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") != "path override")
    }

    private fun checkOsKind(osName: String, osKind: OsKind) {
        System.setProperty("os.name", osName)
        assertEquals(osKind, Jvm.osKind())
    }
}
