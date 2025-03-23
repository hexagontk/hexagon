package com.hexagontk.jul

import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.logging.Level.INFO
import java.util.logging.LogRecord

class SystemStreamHandlerTest {

    @Test fun `Log records can be printed to a stream`() {
        val output = ByteArrayOutputStream()
        val record = LogRecord(INFO, "message")

        SystemStreamHandler(PatternFormat(false), PrintStream(output)).publish(record)
        assert(output.toByteArray().decodeToString().contains("message"))
        output.flush()

        val systemOut = System.out
        System.setOut(PrintStream(output))
        SystemStreamHandler(PatternFormat(false)).publish(record)
        assert(output.toByteArray().decodeToString().contains("message"))
        System.setOut(systemOut)
    }
}
