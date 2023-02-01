package com.hexagonkt.core

import java.net.ServerSocket
import kotlin.test.Test
import java.net.InetAddress
import java.net.URL
import kotlin.test.*

internal class NetworkTest {

    // TODO Replace URL accesses by local started HTTP servers
    @Test fun `Check URL exists`() {
        assert(URL("http://example.com").exists())
        assert(URL("https://example.com").exists())
        assert(URL("file:README.md").exists())
        assert(URL("file:src/test/resources/build.properties").exists())
        assert(URL("classpath:locales/data.json").exists())

        assert(!URL("http://example.com/a.txt").exists())
        assert(!URL("https://example.com/b.html").exists())
        assert(!URL("file:not_existing.txt").exists())
        assert(!URL("file:src").exists())
        assert(!URL("classpath:data.json").exists())
    }

    @Test fun `Check URL first variant`() {
        URL("classpath:locales/data.json").let {
            assertEquals(it, it.firstVariant("_it_IT", "_it"))
        }
        assertEquals(
            URL("classpath:locales/data_en_US.json"),
            URL("classpath:locales/data.json").firstVariant("_en_US", "_en"),
        )
        assertEquals(
            URL("classpath:locales/data_en.json"),
            URL("classpath:locales/data.json").firstVariant("_en_GB", "_en"),
        )
    }

    @Test fun `Check localized URL`() {
        URL("classpath:locales/data.json").let {
            assertEquals(it, it.localized(parseLocale("it_IT")))
        }
        assertEquals(
            URL("classpath:locales/data_en_US.json"),
            URL("classpath:locales/data.json").localized(parseLocale("en_US")),
        )
        assertEquals(
            URL("classpath:locales/data_en.json"),
            URL("classpath:locales/data.json").localized(parseLocale("en_GB")),
        )
    }

    @Test fun `Internet address helper works correctly`() {
        assertEquals(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), allInterfaces)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1)), loopbackInterface)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 3, 2, 1)), inetAddress(127, 3, 2, 1))
    }

    @Test fun `Network ports utilities work properly`() {
        assert(!isPortOpened(freePort()))
        ServerSocket(0).use {
            assert(isPortOpened(it.localPort))
        }
    }

    @Test fun `URL check works properly`() {
        assertTrue { URL("http://example.com").responseSuccessful() }
        assertFalse { URL("http://invalid-domain.z").responseSuccessful() }
        assertTrue { URL("http://example.com").responseFound() }
        assertFalse { URL("http://example.com/nothing").responseFound() }
    }
}
