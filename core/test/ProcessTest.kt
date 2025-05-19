package com.hexagontk.core

import org.junit.jupiter.api.Test

internal class ProcessTest {

    @Test fun `Default JVM info is fetched correctly`() {
        assert(Process.pid > 0)
        assert(Process.uptime() > 0)
    }
}
