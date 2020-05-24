package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import java.lang.management.ManagementFactory
import java.net.Inet4Address

class JvmTest {

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

    @Test fun `System settings with default values are handled properly`() {
        assert(Jvm.systemSetting("this_do_not_exist", "default") == "default")

        System.setProperty("existing_java_property", "value")
        assert(Jvm.systemSetting("existing_java_property", "default") == "value")
    }

    @Test fun `'safeJmx' returns constant when JMX is disabled`() {
        System.setProperty("com.hexagonkt.noJmx", "true")
        assert(Jvm.safeJmx { ManagementFactory.getRuntimeMXBean().name } == "N/A")
        System.clearProperty("com.hexagonkt.noJmx")
    }
}
