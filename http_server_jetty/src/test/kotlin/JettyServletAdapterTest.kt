package com.hexagonkt.http.server.jetty

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class JettyServletAdapterTest {

    @Test fun `Stop method works if called before running`() {
        val adapter = JettyServletAdapter()
        assert(!adapter.started())
        adapter.shutdown()
        assert(!adapter.started())
    }

    @Test fun `Getting the runtime port on stopped instance raises an exception`() {
        assertFailsWith<IllegalStateException> {
            JettyServletAdapter().runtimePort()
        }
    }
}
