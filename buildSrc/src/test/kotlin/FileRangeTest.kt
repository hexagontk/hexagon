

import org.testng.annotations.Test
import java.io.File

class FileRangeTest {

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
        assert(text.contains("/**")) { "Should contains only comment block" }
    }

    @Test fun `Test text with tag present in file`() {
        assert(fileRangeWithTagPresentInFile.text().contains("Service server"))
    }

    @Test fun `Test 'strippedLines' with not ended tag present in file`() {
        assert(fileRangeWithNotEndedTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test 'strippedLines' with no tag present in file`() {
        assert(fileRangeWithNoTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test 'strippedLines' with empty tag present in file`() {
        assert(fileRangeWithEmptyTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test 'strippedLines' with empty tag only comment block present in file`() {
        val strippedLines = fileRangeWithEmptyTagOnlyCommentPresentInFile.strippedLines()
        assert(strippedLines.any { it.contains("/**") }) { "Should contains only comment block" }
    }

    @Test fun `Test 'strippedLines' with tag present in file`() {
        assert(fileRangeWithTagPresentInFile.strippedLines().any { it.contains("Service server") })
    }

    @Test fun `Test 'toString' with not ended tag present in file`() {
        assert(fileRangeWithNotEndedTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test 'toString' with no tag present in file`() {
        assert(fileRangeWithNoTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test 'toString' with empty tag present in file`() {
        assert(fileRangeWithEmptyTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test 'toString' with empty tag only comment block present in file`() {
        assert(fileRangeWithEmptyTagOnlyCommentPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test 'toString' with tag present in file`() {
        assert(fileRangeWithTagPresentInFile.toString().contains(testFile.name))
    }
}
