package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.net.Inet4Address

@Test class JvmTest {

    @Test fun `'hostname' and 'ip' contains valid values` () {
        assert(Jvm.hostname.isNotBlank())
        assert(Jvm.ip.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}")))
        assert(Inet4Address.getAllByName(Jvm.hostname).isNotEmpty())
        assert(Inet4Address.getAllByName(Jvm.ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(Jvm.jvmMemory().matches(numberRegex))
        assert(Jvm.usedMemory().matches(numberRegex))
        assert(Jvm.uptime().matches(numberRegex))
    }
}
