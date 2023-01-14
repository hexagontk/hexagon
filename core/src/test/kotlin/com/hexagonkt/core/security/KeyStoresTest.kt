package com.hexagonkt.core.security

import kotlin.test.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import java.io.File
import java.net.URL
import java.security.cert.X509Certificate
import kotlin.test.assertEquals

internal class KeyStoresTest {

    @Test
    @DisabledOnOs(WINDOWS) // TODO Fails in windows because Algorithm HmacPBESHA256 not available
    fun `Key stores are loaded correctly`() {
        val n = "hexagonkt"
        val f = "$n.p12"
        val pwd = f.reversed()
        val p = if (File("http_test").exists()) "http_test" else "../http_test"

        val ks = loadKeyStore(URL("file:$p/src/main/resources/ssl/$f"), pwd)
        val public = ks.getPublicKey(n)
        val private = ks.getPrivateKey(n, pwd)
        val cert = ks.getCertificate(n) as X509Certificate

        assertEquals("X.509", public.format)
        assertEquals("PKCS#8", private.format)
        assertEquals("CN=Hexagon TEST Root CA,O=Hexagon,C=US", cert.issuerX500Principal.name)
    }
}
