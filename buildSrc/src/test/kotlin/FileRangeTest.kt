import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class FileRangeTest {

    private lateinit var fileRange: FileRange

    @BeforeMethod
    fun setUp() {
        val resourcesDirectory = File("src/test/resources")
        val files = resourcesDirectory.listFiles()
        val testFile = files.single { it.name == "test.md" }
        val testTag = "hello"
        fileRange = FileRange(
            testFile,
            testTag
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

    @Test
    fun testTestToString() {
        assert(fileRange.toString().isNotEmpty())
    }
}
