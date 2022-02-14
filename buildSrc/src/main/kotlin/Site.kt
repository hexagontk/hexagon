
import java.io.File
import java.net.URL

/**
 * Assure that two file ranges are the same (verify that documentation contains only tested code).
 *
 * @param documentationFile File of the documentation to check.
 * @param sourceFile File of the source code that should be included in the documentation.
 * @param tag Tag that defines the text range within the files.
 */
fun checkSampleCode(documentationFile: File, sourceFile: File, tag: String) {
    val fileTag = "// $tag"
    val documentationFileLines = documentationFile.readLines()
    val sourceFileLines = sourceFile.readLines()
    val documentation = documentationFileLines.slice(documentationFileLines.rangeOf(fileTag))
    val source = sourceFileLines.slice(sourceFileLines.rangeOf(fileTag))

    fun List<String>.strippedLines(): List<String> =
        map { it.trim() }.filter { it.isNotEmpty() }

    fun List<String>.text(): String =
        joinToString("\n").trimIndent()

    val documentationLines = documentation.strippedLines()
    if (documentationLines.isNotEmpty() && documentationLines != source.strippedLines())
        error("""
            Documentation $documentation does not match $source

            DOC -----------------------------------------------
            ${documentation.text()}

            SRC -----------------------------------------------
            ${source.text()}
        """.trimIndent())
}

/**
 * Return a text with samples replaced inside.
 *
 * @param parent Base directory used to resolve the files in the content samples.
 * @param content Text with `@code` placeholders to be replaced by actual file ranges.
 * @return The content with the samples inserted in the placeholders.
 */
fun insertSamplesCode(parent: File, content: String): String =
    content.replace("@code (.*)".toRegex()) {
        try {
            val sampleLocation = it.groups[1]?.value?.trim() ?: error("Location expected")
            val url = URL("file:${parent.absolutePath}/$sampleLocation")
            val tag = "// ${url.query}"
            val lines = url.readText().lines()
            val text = lines.slice(lines.rangeOf(tag)).joinToString("\n").trimIndent()
            "```kotlin\n$text\n```"
        } catch (e: Exception) {
            val code = it.value
            println("ERROR: Unable to process '$code' in folder: '${parent.absolutePath}'")
            code
        }
    }

/**
 * Return a text with the proper syntax for the tabbed code blocks feature.
 *
 * @param content Text with incorrect format.
 * @return The content with the tabbed code blocks fixed.
 */
fun fixCodeTabs(content: String): String =
    content
        .replace("""=== "(.*)"\n\n```""".toRegex(), "=== \"$1\"\n")
        .replace("    ```\n```", "    ```")

fun List<String>.rangeOf(tag: String): IntRange {
    val start = indexOfFirst { it.contains(tag) } + 1
    val end = indexOfLast { it.contains(tag) } - 1
    return start .. end
}
