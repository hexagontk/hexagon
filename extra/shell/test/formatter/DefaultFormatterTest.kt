package com.hexagontk.shell.formatter

import com.hexagontk.shell.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultFormatterTest {

    private val formatter = DefaultFormatter()

    @Test fun `Program summary is created properly`() {
        val cmd = Command("cmd")
        assertEquals(
            "cmd - CMD Title (version v1.0)\n\nCmd description",
            formatter.summary(Program("cmd", "v1.0", "CMD Title", "Cmd description"), cmd)
        )
        assertEquals(
            "cmd - CMD Title (version v1.0)",
            formatter.summary(Program("cmd", "v1.0", "CMD Title"), cmd)
        )
        assertEquals(
            "cmd (version v1.0)",
            formatter.summary(Program("cmd", "v1.0"), cmd)
        )
        assertEquals(
            "cmd",
            formatter.summary(Program("cmd"), cmd)
        )
    }
}
