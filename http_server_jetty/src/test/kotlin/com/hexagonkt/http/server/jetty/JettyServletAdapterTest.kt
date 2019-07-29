package com.hexagonkt.http.server.jetty

import org.testng.annotations.Test

@Test class JettyServletAdapterTest {

    @Test fun `Stop method works if called before running`() {
        val adapter = JettyServletAdapter()
        assert(!adapter.started())
        adapter.shutdown()
        assert(!adapter.started())
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Getting the runtime port on stopped instance raises an exception`() {
        JettyServletAdapter().runtimePort()
    }
}
