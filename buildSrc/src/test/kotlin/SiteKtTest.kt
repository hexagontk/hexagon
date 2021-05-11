
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

class SiteKtTest {

    private val resourceFile: File = File("src/test/resources")
    private val testFile: File = File("src/test/resources/test.md")
    private val testFileOut: File = File("src/test/resources/test_out.md")

    @Test fun `Test 'checkSamplesCode'`() {
        checkSamplesCode(FilesRange(testFile, testFileOut, "t"))
        checkSamplesCode(FileRange(testFile, "nothing"), FileRange(testFileOut, "hello"))
    }

    @Test fun `When file ranges don't match 'checkSamplesCode' throws exception`() {
        assertThrows(IllegalStateException::class.java) {
            checkSamplesCode(FileRange(testFile, "hello"), FileRange(testFileOut, "hello"))
        }
    }

    @Test fun `Test insert samples code`() {
        val testTag = "@code test.md:TestMd"
        assert(insertSamplesCode(resourceFile, testTag).contains("kotlin"))
    }

    @Test fun `'fixCodeTabs' reformat code blocks`() {
        val badCodeTabs =
"""=== "build.gradle"

```
    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:hexagon_core:#hexagonVersion")
    ```
```

=== "pom.xml"

```
    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>hexagon_core</artifactId>
      <version>#hexagonVersion</version>
    </dependency>
    ```
```"""

        val expectedCodeTabs =
"""=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:hexagon_core:#hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>hexagon_core</artifactId>
      <version>#hexagonVersion</version>
    </dependency>
    ```"""

        val fixedCodeTabs = fixCodeTabs(badCodeTabs)
        assertEquals(expectedCodeTabs, fixedCodeTabs)
    }

    @Test
    @Disabled
    fun `'addMetadata' inserts proper edit link`() {

        val project = ProjectBuilder.builder().withProjectDir(File("build/resources/test")).build()
        val base = project.projectDir

        fun editLine(path: String, file: String) {
            val firstLine = base.resolve(path).readLines().first()
            println(">>>>>>>>>>>>>>>>>> Path: $path File: $file First Line: $firstLine")
            assert(firstLine.startsWith("edit_url: edit/develop/"))
            assert(firstLine.removePrefix("edit_url: edit/develop/") ==  file)
        }

        println(">>>>>>>>>>>>>>>>>> Adding metadata...")
        project.addMetadata(base.absolutePath)
        println(">>>>>>>>>>>>>>>>>> Metadata ADDED")

        editLine("test_out.md", "hexagon_site/pages/test_out.md")
        editLine("test.md", "hexagon_site/pages/test.md")
        editLine("examples/example_projects.md", "hexagon_site/pages/examples/example_projects.md")
        editLine("hexagon_core/hexagon_core.md", "hexagon_core/README.md")
        editLine("hexagon_core/index.md", "hexagon_core/README.md")
        editLine("hexagon_core/alltypes/index.md", "hexagon_core/README.md")
        editLine("hexagon_core/com.hexagonkt.serialization/index.md", "hexagon_core/README.md")
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-json.md",
            "hexagon_core/src/main/kotlin/serialization/Json.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/parse.md",
            "hexagon_core/src/main/kotlin/parse.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/index.md",
            "hexagon_core/src/test/kotlin/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/parse.md",
            "hexagon_core/src/test/kotlin/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv-test/content-type.md",
            "hexagon_core/src/test/kotlin/serialization/CsvTest.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/index.md",
            "hexagon_core/src/main/kotlin/serialization/Csv.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/parse.md",
            "hexagon_core/src/main/kotlin/serialization/Csv.kt"
        )
        editLine(
            "hexagon_core/com.hexagonkt.serialization/-csv/content-type.md",
            "hexagon_core/src/main/kotlin/serialization/Csv.kt"
        )
    }
}
