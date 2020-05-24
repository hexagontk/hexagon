package com.hexagonkt.http.server.jetty

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class JettyServletAdapterTest {

    @Test fun `Stop method works if called before running`() {
        val adapter = JettyServletAdapter()
        assert(!adapter.started())
        adapter.shutdown()
        assert(!adapter.started())
    }

    @Test fun `Getting the runtime port on stopped instance raises an exception`() {
        shouldThrow<IllegalStateException> {
            JettyServletAdapter().runtimePort()
        }
    }
}
