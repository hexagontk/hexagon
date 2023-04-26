package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.toHttpFormat
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DateCallbackTest {

    @Test fun `Date callback creates the proper response`() {
        val context = HttpContext()
        val instant = Instant.now()
        val date = DateCallback()(context).response.headers.require("date").valueString() ?: fail
        assertEquals(instant.toHttpFormat().substringBeforeLast(':'), date.substringBeforeLast(':'))
    }
}
