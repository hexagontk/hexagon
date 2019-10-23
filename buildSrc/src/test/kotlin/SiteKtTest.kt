
import org.testng.annotations.Test
import java.io.File

class SiteKtTest {

    private val resourceFile: File = File("src/test/resources")
    private val testFile = File("src/test/resources/test.md")
    private val testFileOut = File("src/test/resources/test_out.md")

    @Test fun `Test checkSamplesCode`() {
        checkSamplesCode(FilesRange(testFile, testFileOut, "t"))
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `When file ranges don't match checkSamplesCode throws exception`() {
        checkSamplesCode(FileRange(testFile, "hello"), FileRange(testFileOut, "hello"))
    }

    @Test fun `Test insert samples code`() {
        val testTag = "@sample test.md:TestMd"
        assert(insertSamplesCode(resourceFile, testTag).contains("kotlin"))
    }
}
