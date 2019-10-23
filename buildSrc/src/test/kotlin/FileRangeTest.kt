

import org.testng.annotations.Test
import java.io.File

class FileRangeTest {

    private val testFile = File("src/test/resources/test.md")

    private val fileRangeWithTagPresentInFile: FileRange = testFileRange("hello")
    private val fileRangeWithNoTagPresentInFile: FileRange = testFileRange("world")
    private val fileRangeWithNotEndedTagPresentInFile: FileRange = testFileRange("tagNotEnded")
    private val fileRangeWithEmptyTagPresentInFile: FileRange =
        testFileRange("emptyTagWithNoContent")
    private val fileRangeWithEmptyTagOnlyCommentPresentInFile: FileRange =
        testFileRange("emptyTagWithOnlyCommentBlock")

    private fun testFileRange(tag: String) =
        FileRange(testFile, tag)

    @Test fun `Test text with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.text().isEmpty())
    }

    @Test fun `Test text with Empty tag Only Comment Block present in File`() {
        val text = fileRangeWithEmptyTagOnlyCommentPresentInFile.text()
        assert(text.contains("/**")) { "Should contains only comment block" }
    }

    @Test fun `Test text with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.text().contains("Service server"))
    }

    @Test fun `Test strippedLines with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test strippedLines with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test strippedLines with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.strippedLines().isEmpty())
    }

    @Test fun `Test strippedLines with Empty tag Only Comment Block present in File`() {
        val strippedLines = fileRangeWithEmptyTagOnlyCommentPresentInFile.strippedLines()
        assert(strippedLines.any { it.contains("/**") }) { "Should contains only comment block" }
    }

    @Test fun `Test strippedLines with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.strippedLines().any { it.contains("Service server") })
    }

    @Test fun `Test toString with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test toString with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test toString with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test toString with Empty tag Only Comment Block present in File`() {
        assert(fileRangeWithEmptyTagOnlyCommentPresentInFile.toString().contains(testFile.name))
    }

    @Test fun `Test toString with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.toString().contains(testFile.name))
    }
}
