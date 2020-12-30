package logging.jul

import com.hexagonkt.logging.jul.PatternFormat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.logging.Level.INFO
import java.util.logging.LogRecord

internal class PatternFormatTest {

    @Test fun `Formatting messages with 'printf' special characters works correctly`() {
        val format = PatternFormat()
        val message = "Message with '%'"
        Assertions.assertTrue(format.format(LogRecord(INFO, message)).contains(message))
    }
}
