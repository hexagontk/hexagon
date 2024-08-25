package com.hexagontk.core.security

import com.hexagontk.core.urlOf
import org.junit.jupiter.api.Test
import java.io.File
import java.security.cert.X509Certificate
import kotlin.test.assertEquals

internal class KeyStoresTest {

    @Test fun `Key stores are loaded correctly`() {
        val n = "hexagontk"
        val f = "$n.p12"
        val pwd = f.reversed()
        val p = if (File("http_test").exists()) "http/http_test" else "../http/http_test"

        val ks = loadKeyStore(urlOf("file:$p/src/main/resources/ssl/$f"), pwd)
        val public = ks.getPublicKey(n)
        val private = ks.getPrivateKey(n, pwd)
        val cert = ks.getCertificate(n) as X509Certificate

        assertEquals("X.509", public.format)
        assertEquals("PKCS#8", private.format)
        assertEquals("CN=Hexagon TEST Root CA,O=Hexagon,C=US", cert.issuerX500Principal.name)
    }
}
