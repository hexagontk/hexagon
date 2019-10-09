import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class SiteKtTest {

    private lateinit var resourceFile: File
    private lateinit var testFile: File
    private lateinit var testFileOut: File

    @BeforeMethod
    fun setUp() {
        resourceFile = File("src/test/resources")
        val files = resourceFile.listFiles()
        testFile = files.single { it.name == "test.md"}
        testFileOut = files.single { it.name == "test_out.md"}
    }

    @Test
    fun testCheckSamplesCode() {
        checkSamplesCode(FilesRange(testFile, testFileOut, "t"))
    }

    @Test(expectedExceptions = [IllegalStateException::class])
    fun testTestCheckSamplesCode() {
        checkSamplesCode(FileRange(testFile, "hello"), FileRange(testFileOut, "hello"))
    }

    @Test
    fun testInsertSamplesCode() {
        val testTag = "@sample test.md:TestMd"
        assert(insertSamplesCode(resourceFile, testTag).contains("kotlin"))
    }
}
