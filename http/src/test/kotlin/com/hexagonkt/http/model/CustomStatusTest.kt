package com.hexagonkt.http.model

import com.hexagonkt.core.disableChecks
import com.hexagonkt.http.model.HttpStatusType.*
import kotlin.test.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class CustomStatusTest {

    @Test fun `Custom status codes gets the proper type`() {
        CustomStatus(100).let {
            assertEquals(100, it.code)
            assertEquals(INFORMATION, it.type)
        }
        CustomStatus(199).let {
            assertEquals(199, it.code)
            assertEquals(INFORMATION, it.type)
        }
        CustomStatus(200).let {
            assertEquals(200, it.code)
            assertEquals(SUCCESS, it.type)
        }
        CustomStatus(299).let {
            assertEquals(299, it.code)
            assertEquals(SUCCESS, it.type)
        }
        CustomStatus(300).let {
            assertEquals(300, it.code)
            assertEquals(REDIRECTION, it.type)
        }
        CustomStatus(399).let {
            assertEquals(399, it.code)
            assertEquals(REDIRECTION, it.type)
        }
        CustomStatus(400).let {
            assertEquals(400, it.code)
            assertEquals(CLIENT_ERROR, it.type)
        }
        CustomStatus(499).let {
            assertEquals(499, it.code)
            assertEquals(CLIENT_ERROR, it.type)
        }
        CustomStatus(500).let {
            assertEquals(500, it.code)
            assertEquals(SERVER_ERROR, it.type)
        }
        CustomStatus(599).let {
            assertEquals(599, it.code)
            assertEquals(SERVER_ERROR, it.type)
        }
    }

    @Test fun `Custom status codes can override their proper type`() {
        CustomStatus(100, SUCCESS).let {
            assertEquals(100, it.code)
            assertEquals(SUCCESS, it.type)
        }
        CustomStatus(200, INFORMATION).let {
            assertEquals(200, it.code)
            assertEquals(INFORMATION, it.type)
        }
    }

    @Test fun `Invalid custom status codes throw exceptions on creation`() {
        assertFailsWith<IllegalArgumentException> { CustomStatus(99, INFORMATION)  }
        assertFailsWith<IllegalArgumentException> { CustomStatus(600, INFORMATION)  }
        assertFailsWith<IllegalArgumentException> { CustomStatus(99)  }
        assertFailsWith<IllegalArgumentException> { CustomStatus(600)  }
    }

    @Test fun `Checks are skipped in production mode`() {
        disableChecks = true
        CustomStatus(99, INFORMATION)
        CustomStatus(600, INFORMATION)
        disableChecks = false
    }
}
