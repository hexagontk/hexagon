import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class FileRangeTest {

    private lateinit var fileRangeWithTagPresentInFile: FileRange
    private lateinit var fileRangeWithNoTagPresentInFile: FileRange
    private lateinit var fileRangeWithNotEndedTagPresentInFile: FileRange
    private lateinit var fileRangeWithEmptyTagPresentInFile: FileRange
    private lateinit var fileRangeWithEmptyTagOnlyCommentPresentInFile: FileRange
    private lateinit var testFile: File
    private lateinit var tagPresentInFile: String
    private lateinit var tagNotPresentInFile: String
    private lateinit var tagNotEndedInFile: String
    private lateinit var tagEmptyInFile: String
    private lateinit var tagEmptyOnlyComment: String

    @BeforeMethod
    fun setUp() {
        val resourcesDirectory = File("src/test/resources")
        val files = resourcesDirectory.listFiles()
        testFile = files.single { it.name == "test.md" }

        tagNotPresentInFile = "world"
        fileRangeWithNoTagPresentInFile = FileRange(
            testFile,
            tagNotPresentInFile
        )

        tagNotEndedInFile = "tagNotEnded"
        fileRangeWithNotEndedTagPresentInFile = FileRange(
            testFile,
            tagNotEndedInFile
        )

        tagEmptyInFile = "emptyTagWithNoContent"
        fileRangeWithEmptyTagPresentInFile = FileRange(
            testFile,
            tagEmptyInFile
        )

        tagEmptyOnlyComment = "emptyTagWithOnlyCommentBlock"
        fileRangeWithEmptyTagOnlyCommentPresentInFile = FileRange(
            testFile,
            tagEmptyOnlyComment
        )

        tagPresentInFile = "hello"
        fileRangeWithTagPresentInFile = FileRange(
            testFile,
            tagPresentInFile
        )
    }

    @Test
    fun `test text with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.text().isEmpty())
    }

    @Test
    fun `test text with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.text().isEmpty())
    }

    @Test
    fun `test text with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.text().isEmpty())
    }

    @Test
    fun `test text with Empty tag Only Comment Block present in File`() {
        assert(fileRangeWithEmptyTagOnlyCommentPresentInFile.text().contains("/**")) { "Should contains only comment block" }
    }

    @Test
    fun `test text with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.text().contains("Service server"))
    }

    @Test
    fun `test strippedLines with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.strippedLines().isEmpty())
    }

    @Test
    fun `test strippedLines with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.strippedLines().isEmpty())
    }

    @Test
    fun `test strippedLines with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.strippedLines().isEmpty())
    }

    @Test
    fun `test strippedLines with Empty tag Only Comment Block present in File`() {
        assert(fileRangeWithEmptyTagOnlyCommentPresentInFile.strippedLines().any { it.contains("/**") }) { "Should contains only comment block" }
    }

    @Test
    fun `test strippedLines with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.strippedLines().any { it.contains("Service server") })
    }

    @Test
    fun `test toString with Not Ended tag present in File`() {
        assert(fileRangeWithNotEndedTagPresentInFile.toString().contains(testFile.name))
    }

    @Test
    fun `test toString with No tag present in File`() {
        assert(fileRangeWithNoTagPresentInFile.toString().contains(testFile.name))
    }

    @Test
    fun `test toString with Empty tag present in File`() {
        assert(fileRangeWithEmptyTagPresentInFile.toString().contains(testFile.name))
    }

    @Test
    fun `test toString with Empty tag Only Comment Block present in File`() {
        assert(fileRangeWithEmptyTagOnlyCommentPresentInFile.toString().contains(testFile.name))
    }

    @Test
    fun `test toString with tag present in File`() {
        assert(fileRangeWithTagPresentInFile.toString().contains(testFile.name))
    }
}
