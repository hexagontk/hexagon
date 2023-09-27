package com.hexagonkt.core

import java.net.ServerSocket
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.util.*
import kotlin.test.*

internal class NetworkTest {

    @Test fun `Network utilities`() {
        // network
        // TODO
        // network
    }

    // TODO Replace URL accesses by local started HTTP servers
    @Test fun `Check URL exists`() {
        assert(urlOf("http://example.com").exists())
        assert(urlOf("https://example.com").exists())
        assert(urlOf("file:README.md").exists())
        assert(urlOf("file:src/test/resources/build.properties").exists())
        assert(urlOf("classpath:locales/data.json").exists())

        assert(!urlOf("http://example.com/a.txt").exists())
        assert(!urlOf("https://example.com/b.html").exists())
        assert(!urlOf("file:not_existing.txt").exists())
        assert(!urlOf("file:src").exists())
        assert(!urlOf("classpath:data.json").exists())

        assert(!urlOf("ftp://example.com").exists())
    }

    @Test fun `Check URL first variant`() {
        urlOf("classpath:locales/data.json").let {
            assertEquals(it, it.firstVariant("_it_IT", "_it"))
        }
        assertEquals(
            urlOf("classpath:locales/data_en_US.json"),
            urlOf("classpath:locales/data.json").firstVariant("_en_US", "_en"),
        )
        assertEquals(
            urlOf("classpath:locales/data_en.json"),
            urlOf("classpath:locales/data.json").firstVariant("_en_GB", "_en"),
        )
    }

    @Test fun `Check localized URL`() {
        fun locale(language: String, region: String): Locale =
            Locale.Builder().setLanguage(language).setRegion(region).build()

        urlOf("classpath:locales/data.json").let {
            assertEquals(it, it.localized(locale("it", "IT")))
        }
        assertEquals(
            urlOf("classpath:locales/data_en_US.json"),
            urlOf("classpath:locales/data.json").localized(locale("en", "US")),
        )
        assertEquals(
            urlOf("classpath:locales/data_en.json"),
            urlOf("classpath:locales/data.json").localized(locale("en", "GB")),
        )
    }

    @Test fun `Internet address helper works correctly`() {
        assertEquals(InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0)), ALL_INTERFACES)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1)), LOOPBACK_INTERFACE)
        assertEquals(InetAddress.getByAddress(byteArrayOf(127, 3, 2, 1)), inetAddress(127, 3, 2, 1))
    }

    @Test fun `Network ports utilities work properly`() {
        assert(!isPortOpened(freePort()))
        ServerSocket(0).use {
            assert(isPortOpened(it.localPort))
        }
    }

    @Test fun `URL check works properly`() {
        assertTrue { urlOf("http://example.com").responseSuccessful() }
        assertFalse { urlOf("http://invalid-domain.z").responseSuccessful() }
        assertTrue { urlOf("http://example.com").responseFound() }
        assertFalse { urlOf("http://example.com/nothing").responseFound() }
    }
}
