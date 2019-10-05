import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class FileRangeTest {

    private lateinit var fileRange: FileRange

    @BeforeMethod
    fun setUp() {
        val resourcesDirectory = File("src/test/resources")
        val files = resourcesDirectory.listFiles()
        val testFile = files[0]

        fileRange = FileRange(
            testFile,
        "hello"
        )
    }

    @Test
    fun testText() {
        assert(fileRange.text().contains("\n"))
    }

    @Test
    fun testStrippedLines() {
        assert(fileRange.strippedLines().isNotEmpty())
    }
}
