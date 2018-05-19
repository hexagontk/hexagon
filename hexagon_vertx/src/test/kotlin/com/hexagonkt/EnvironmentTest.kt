package com.hexagonkt

import com.hexagonkt.Environment.hostname
import com.hexagonkt.Environment.ip
import com.hexagonkt.Environment.jvmMemory
import com.hexagonkt.Environment.uptime
import com.hexagonkt.Environment.usedMemory
import org.junit.Test
import java.net.Inet4Address

class EnvironmentTest {

    @Test fun `'hostname' and 'ip' contains valid values` () {
        assert(hostname.isNotBlank())
        assert(ip.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}")))
        assert(Inet4Address.getAllByName(hostname).isNotEmpty())
        assert(Inet4Address.getAllByName(ip).isNotEmpty())
    }

    @Test fun `JVM metrics have valid values` () {
        val numberRegex = Regex("[\\d.,]+")
        assert(jvmMemory().matches(numberRegex))
        assert(usedMemory().matches(numberRegex))
        assert(uptime().matches(numberRegex))
    }
}
