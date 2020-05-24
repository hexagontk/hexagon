
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Assertions.*
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class SiteKtTest {

    private val resourceFile: File = File("src/test/resources")
    private val testFile: File = File("src/test/resources/test.md")
    private val testFileOut: File = File("src/test/resources/test_out.md")

    @Test fun `Test 'checkSamplesCode'`() {
        checkSamplesCode(FilesRange(testFile, testFileOut, "t"))
    }

    @Test fun `When file ranges don't match 'checkSamplesCode' throws exception`() {
        assertThrows(IllegalStateException::class.java) {
            checkSamplesCode(FileRange(testFile, "hello"), FileRange(testFileOut, "hello"))
        }
    }

    @Test fun `Test insert samples code`() {
        val testTag = "@sample test.md:TestMd"
        assert(insertSamplesCode(resourceFile, testTag).contains("kotlin"))
    }

    @Test
    @Disabled // TODO Fix this test using ../hexagon_core as the mock project
    fun `'addMetadata' inserts proper edit link`() {

        fun prj(path: String) =
            ProjectBuilder.builder().withProjectDir(File(path)).build()

        fun prj(path: String, parent: Project) =
            ProjectBuilder.builder().withProjectDir(File(path))
                .withParent(parent)
                .build()

        val rp = prj("build/resources/test")
        val cp = prj("build/resources/test/hexagon_core", rp)
        val sp = prj("build/resources/test/examples", rp)
        println ("&&&&& " + cp.parent)
        rp.subprojects.add(cp)
//        rp.subprojects.add(prj("build/resources/test/hexagon_core", rp))
        println(">>>>>>>>>>>> ${rp.projectDir.absolutePath}")
        println(">>>>>>>>>>>> ${sp.projectDir.absolutePath}")
        rp.subprojects.forEach { println("######### ${it.projectDir.absolutePath}")}
        val base = rp.projectDir

        fun editLine(path: String, file: String) {
            val firstLine = base.resolve(path).readLines().first()
            assert(firstLine.startsWith("edit_url: edit/develop/"))
            assert(firstLine.removePrefix("edit_url: edit/develop/") ==  file)
        }

        sp.addMetadata(base.absolutePath)

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

    @Test fun `'classPackage' returns the correct package directory`() {

        ProjectBuilder.builder().withProjectDir(File("build/resources/test/hexagon_core")).build().let {
            assert(it.classPackage("main", "Jvm.kt") == "helpers/")
            assert(it.classPackage("test", "SettingsTest.kt") == "settings/")
            assert(it.classPackage("main", "CronScheduler.kt") == "")
            assert(it.classPackage("test", "CronSchedulerTest.kt") == "")
        }
    }
}
