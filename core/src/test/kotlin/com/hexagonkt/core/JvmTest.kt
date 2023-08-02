package com.hexagonkt.core

import kotlin.test.Test
import kotlin.IllegalArgumentException
import java.net.Inet4Address
import java.net.URI
import kotlin.test.*

internal class JvmTest {

    @Test fun `'systemFlag' fails with a blank setting name`() {
        assertFailsWith<IllegalArgumentException> { Jvm.systemFlag("") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemFlag(" ") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>("") }
        assertFailsWith<IllegalArgumentException> { Jvm.systemSettingOrNull<String>(" ") }
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
        assertNull(Jvm.systemSettingOrNull<String>("_not_defined_"))

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") == "path override")
    }
}
