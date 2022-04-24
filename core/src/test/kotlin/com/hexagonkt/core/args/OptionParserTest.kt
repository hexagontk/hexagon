package com.hexagonkt.core.args

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OptionParserTest {

    private lateinit var optionParser: OptionParser

    @BeforeEach fun setUp() {
        optionParser = OptionParser()
    }

    @Test fun `Parse string option`() {
        data class Args(val verbose: Boolean): Arguments

        val actual = optionParser.parse(Args::class, arrayOf("--verbose", "true"))
        val expected = Args(verbose = true)

        assertEquals(Result.success(expected), actual)
    }
}
