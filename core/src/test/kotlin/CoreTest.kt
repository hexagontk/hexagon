package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

internal class CoreTest {

    @Test fun `Production mode is disabled by default`() {
        assertFalse(disableChecks)
    }
}
