
import org.junit.jupiter.api.Test
import java.io.File

internal class FileRangeTest {

    private val testFile: File = File("src/test/resources/test.md")

    private val fileRangeWithTagPresentInFile: FileRange = testFileRange("hello")
    private val fileRangeWithNoTagPresentInFile: FileRange = testFileRange("world")
    private val fileRangeWithNotEndedTagPresentInFile: FileRange = testFileRange("tagNotEnded")
    private val fileRangeWithEmptyTagPresentInFile: FileRange =
        testFileRange("emptyTagWithNoContent")
    private val fileRangeWithEmptyTagOnlyCommentPresentInFile: FileRange =
        testFileRange("emptyTagWithOnlyCommentBlock")

    private fun testFileRange(tag: String): FileRange =
        FileRange(testFile, tag)

    @Test fun `Test text with not ended tag present in file`() {
        assert(fileRangeWithNotEndedTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with no tag present in file`() {
        assert(fileRangeWithNoTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with empty tag present in file`() {
        assert(fileRangeWithEmptyTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with empty tag only comment block present in file`() {
        val text = fileRangeWithEmptyTagOnlyCommentPresentInFile.text()
        assert(text.contains("/**")) { "Should contain only comment block" }
    }

    @Test fun `Test text with tag present in file`() {
        assert(fileRangeWithTagPresentInFile.text().contains("Service server"))
    }
}
