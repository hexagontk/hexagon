
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test
import java.io.File

class SiteKtTest {

    private val resourceFile: File = File("src/test/resources")
    private val testFile: File = File("src/test/resources/test.md")
    private val testFileOut: File = File("src/test/resources/test_out.md")

    @Test fun `Test 'checkSamplesCode'`() {
        checkSamplesCode(FilesRange(testFile, testFileOut, "t"))
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `When file ranges don't match 'checkSamplesCode' throws exception`() {
        checkSamplesCode(FileRange(testFile, "hello"), FileRange(testFileOut, "hello"))
    }

    @Test fun `Test insert samples code`() {
        val testTag = "@sample test.md:TestMd"
        assert(insertSamplesCode(resourceFile, testTag).contains("kotlin"))
    }

    @Test fun `'addMetadata' inserts proper edit link`() {

        val project = ProjectBuilder.builder().withProjectDir(File("build")).build()
        val base = File("build/resources/test")

        fun editLine(path: String, file: String) {
            val firstLine = base.resolve(path).readLines().first()
            assert(firstLine.startsWith("edit_url: edit/develop/"))
            assert(firstLine.removePrefix("edit_url: edit/develop/") ==  file)
        }

        addMetadata(base.absolutePath, project)

        editLine("test_out.md", "hexagon_site/pages/test_out.md")
        editLine("test.md", "hexagon_site/pages/test.md")
        editLine("examples/example_projects.md", "hexagon_site/pages/examples/example_projects.md")
        editLine("hexagon_core/hexagon_core.md", "hexagon_core/README.md")
        editLine("hexagon_core/index.md", "hexagon_core/README.md")
        editLine("hexagon_core/alltypes/index.md", "hexagon_core/README.md")
        editLine("hexagon_core/com.hexagonkt.serialization/index.md", "hexagon_core/README.md")
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-json.md",
            "hexagon_core/src/main/kotlin/com/hexagonkt/serialization/Json.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/parse.md",
            "hexagon_core/src/main/kotlin/com/hexagonkt/serialization/parse.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/index.md",
            "hexagon_core/src/test/kotlin/com/hexagonkt/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/parse.md",
            "hexagon_core/src/test/kotlin/com/hexagonkt/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/content-type.md",
            "hexagon_core/src/test/kotlin/com/hexagonkt/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/index.md",
            "hexagon_core/src/main/kotlin/com/hexagonkt/serialization/Csv.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/parse.md",
            "hexagon_core/src/main/kotlin/com/hexagonkt/serialization/Csv.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/content-type.md",
            "hexagon_core/src/main/kotlin/com/hexagonkt/serialization/Csv.kt"
        )
    }
}
