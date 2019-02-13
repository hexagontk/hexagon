package serialization

import com.hexagonkt.serialization.ParseException
import org.testng.annotations.Test
import java.lang.IllegalStateException

class ParseExceptionTest {
    @Test fun `ParseException obtains field name properly`() {
        assert(ParseException(null).field == "")
        assert(ParseException(IllegalStateException()).field == "")
    }
}
