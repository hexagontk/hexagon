package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.require
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.toHttpFormat
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DateCallbackTest {

    @Test fun `Date callback creates the proper response`() {
        val instant = Instant.now().toHttpFormat()
        val date = DateCallback()(HttpServerContext()).response.headers.require("date").value
        assertEquals(instant, date)
    }
}
