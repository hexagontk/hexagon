package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.net.Inet4Address

@Test class EnvironmentTest {

    @Test fun `'hostname' and 'ip' contains valid values` () {
        assert(Environment.hostname.isNotBlank())
        assert(Environment.ip.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}")))
        assert(Inet4Address.getAllByName(Environment.hostname).isNotEmpty())
        assert(Inet4Address.getAllByName(Environment.ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(Environment.jvmMemory().matches(numberRegex))
        assert(Environment.usedMemory().matches(numberRegex))
        assert(Environment.uptime().matches(numberRegex))
    }
}
