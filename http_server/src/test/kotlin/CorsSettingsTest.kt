package com.hexagonkt.http.server

import com.hexagonkt.http.ALL
import com.hexagonkt.http.Method.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CorsSettingsTest {

    @Test fun `Default values are returned if created without parameters`() {
        val settings = CorsSettings()

        assert(settings.allowedOrigin.pattern == ".*")
        assert(settings.allowedMethods == ALL)
        assert(settings.allowedHeaders == emptySet<String>())
        assert(settings.exposedHeaders == emptySet<String>())
        assert(settings.supportCredentials)
        assert(settings.preFlightStatus == 204)
        assert(settings.preFlightMaxAge == 0L)

        val globSettings = CorsSettings(
            "example.org",
            setOf(GET, HEAD),
            setOf("h1", "h2"),
            setOf("eh1", "eh2"),
            false,
            222,
            1
        )

        val regexSettings = CorsSettings(
            Regex("example\\.org"),
            setOf(GET, HEAD),
            setOf("h1", "h2"),
            setOf("eh1", "eh2"),
            false,
            222,
            1
        )

        assertEquals(globSettings.allowedOrigin.pattern, regexSettings.allowedOrigin.pattern)
        assertEquals(globSettings.allowedMethods, regexSettings.allowedMethods)
        assertEquals(globSettings.allowedHeaders, regexSettings.allowedHeaders)
        assertEquals(globSettings.exposedHeaders, regexSettings.exposedHeaders)
        assertEquals(globSettings.supportCredentials, regexSettings.supportCredentials)
        assertEquals(globSettings.preFlightStatus, regexSettings.preFlightStatus)
        assertEquals(globSettings.preFlightMaxAge, regexSettings.preFlightMaxAge)
    }

    @Test fun `Allow origin handles wildcards correctly`() {
        val settings = CorsSettings("*.example.org")

        assert(settings.allowOrigin("alpha.example.org"))
        assert(settings.allowOrigin("beta.example.org"))
        assert(settings.allowOrigin(".example.org"))
        assert(!settings.allowOrigin("example.org"))
        assert(!settings.allowOrigin("alpha.example.com"))
    }

    @Test fun `Access control allow origin is processed properly`() {
        CorsSettings().apply {
            assert(accessControlAllowOrigin("example.org") == "example.org")
            assert(accessControlAllowOrigin("domain.org") == "domain.org")
        }

        CorsSettings(supportCredentials = false).apply {
            assert(accessControlAllowOrigin("example.org") == "*")
            assert(accessControlAllowOrigin("domain.org") == "*")
        }

        CorsSettings("example.org").apply {
            assert(accessControlAllowOrigin("example.org") == "example.org")
        }
    }
}
