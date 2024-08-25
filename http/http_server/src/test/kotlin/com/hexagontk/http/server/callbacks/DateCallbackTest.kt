package com.hexagontk.http.server.callbacks

import com.hexagontk.core.fail
import com.hexagontk.core.require
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.toHttpFormat
import java.time.Instant
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DateCallbackTest {

    @Test fun `Date callback creates the proper response`() {
        val context = HttpContext()
        val instant = Instant.now()
        val date = DateCallback()(context).response.headers.require("date").string() ?: fail
        assertEquals(instant.toHttpFormat().substringBeforeLast(':'), date.substringBeforeLast(':'))
    }
}
