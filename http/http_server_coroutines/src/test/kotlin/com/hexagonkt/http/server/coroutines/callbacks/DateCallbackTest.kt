package com.hexagonkt.http.server.coroutines.callbacks

import com.hexagonkt.core.fail
import com.hexagonkt.core.require
import com.hexagonkt.http.handlers.coroutines.HttpContext
import com.hexagonkt.http.toHttpFormat
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DateCallbackTest {

    @Test fun `Date callback creates the proper response`() = runBlocking {
        val context = HttpContext()
        val instant = Instant.now()
        val date = DateCallback()(context).response.headers.require("date").string() ?: fail
        assertEquals(instant.toHttpFormat().substringBeforeLast(':'), date.substringBeforeLast(':'))
    }
}
