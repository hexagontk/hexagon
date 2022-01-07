
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

internal class SiteKtTest {

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

    @Test fun `Insert samples code does not fail on not found files`() {
        val testTag = "@code test_not_found.md:TestMd"
        assertEquals(testTag, insertSamplesCode(resourceFile, testTag))
    }

    @Test fun `'fixCodeTabs' reformat code blocks`() {
        val badCodeTabs =
"""=== "build.gradle"

```
    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:core:#hexagonVersion")
    ```
```

=== "pom.xml"

```
    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>core</artifactId>
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

    implementation("com.hexagonkt:core:#hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>core</artifactId>
      <version>#hexagonVersion</version>
    </dependency>
    ```"""

        val fixedCodeTabs = fixCodeTabs(badCodeTabs)
        assertEquals(expectedCodeTabs, fixedCodeTabs)
    }
}
