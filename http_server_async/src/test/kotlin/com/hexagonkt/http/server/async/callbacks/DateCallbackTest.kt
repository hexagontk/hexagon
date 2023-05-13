package com.hexagonkt.http.server.async.callbacks

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.handlers.async.HttpContext
import com.hexagonkt.http.toHttpFormat
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DateCallbackTest {

    @Test fun `Date callback creates the proper response`() {
        val context = HttpContext()
        val instant = Instant.now()
        val date = DateCallback()(context).join().response.headers.require("date").value ?: fail
        assertEquals(instant.toHttpFormat().substringBeforeLast(':'), date.substringBeforeLast(':'))
    }
}
