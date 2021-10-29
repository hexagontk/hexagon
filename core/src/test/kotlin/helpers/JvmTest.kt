package com.hexagonkt.core.helpers

import com.hexagonkt.core.helpers.Jvm.NO_JMX_ERROR
import org.junit.jupiter.api.Test
import java.lang.management.ManagementFactory
import java.net.Inet4Address
import kotlin.test.*

internal class JvmTest {

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
        assert(Jvm.hostname.isNotBlank())
        assert(Inet4Address.getAllByName(Jvm.hostname).isNotEmpty())
        assert(Inet4Address.getAllByName(Jvm.ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(Jvm.initialMemory().matches(numberRegex))
        assert(Jvm.usedMemory().matches(numberRegex))
        assert(Jvm.uptime().matches(numberRegex))
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
        System.setProperty("invalidBoolean", "_")
        System.setProperty("invalidInt", "_")
        System.setProperty("invalidLong", "_")
        System.setProperty("invalidFloat", "_")
        System.setProperty("invalidDouble", "_")
        System.setProperty("string", "text")
        System.setProperty("error", "value")

        assertEquals(true, Jvm.systemSetting("validBoolean"))
        assertEquals(123, Jvm.systemSetting("validInt"))
        assertEquals(456L, Jvm.systemSetting("validLong"))
        assertEquals(0.5F, Jvm.systemSetting("validFloat"))
        assertEquals(1.5, Jvm.systemSetting("validDouble"))

        assertNull(Jvm.systemSetting<Boolean>("invalidBoolean"))
        assertNull(Jvm.systemSetting<Boolean>("invalidInt"))
        assertNull(Jvm.systemSetting<Boolean>("invalidLong"))
        assertNull(Jvm.systemSetting<Boolean>("invalidFloat"))
        assertNull(Jvm.systemSetting<Boolean>("invalidDouble"))

        assertEquals("text", Jvm.systemSetting("string"))

        val type = System::class
        val e = assertFailsWith<IllegalStateException> { Jvm.systemSetting<System>("error") }
        assertEquals("Setting: 'error' has unsupported type: ${type.qualifiedName}", e.message)
    }

    @Test fun `Default charset is fetched correctly`() {
        assert(Jvm.charset.isRegistered)
        assert(Jvm.charset.canEncode())
    }

    @Test fun `'safeJmx' returns constant when JMX is disabled`() {
        System.setProperty("com.hexagonkt.noJmx", "true")
        assert(Jvm.safeJmx { ManagementFactory.getRuntimeMXBean().name } == "N/A")
        System.clearProperty("com.hexagonkt.noJmx")
    }

    @Test fun `'safeJmx' gives proper information on exceptions`() {
        System.clearProperty("com.hexagonkt.noJmx")

        val exception = assertFailsWith<IllegalStateException> {
            (Jvm.safeJmx { error("Internal error") } == "N/A")
        }

        val cause = exception.cause

        assert(cause is IllegalStateException)
        assertEquals("Internal error", cause?.message)
        assertEquals(NO_JMX_ERROR, exception.message)
    }
}
