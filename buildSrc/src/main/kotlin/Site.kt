
import java.io.File
import java.net.URL

/**
 * Set of lines inside a text file.
 *
 * @param file Text file holding the lines.
 * @param range Range of lines inside the supplied file.
 */
class FileRange(private val file: File, private val range: IntRange) {

    constructor(file: File, tag: String) : this(
        file,
        file
            .readLines()
            .let { lines ->
                val start = lines.indexOfFirst { it.contains("// $tag") } + 1
                val end = lines.indexOfLast { it.contains("// $tag") } - 1
                start .. end
            }
    )

    fun text(): String =
        lines().joinToString("\n").trimIndent()

    fun lines(): List<String> =
        file.readLines().slice(range)
}

/**
 * Assure that two file ranges are the same (verify that documentation contains only tested code).
 *
 * @param documentationFile File of the documentation to check.
 * @param sourceFile File of the source code that should be included in the documentation.
 * @param tag Tag that defines the text range within the files.
 */
fun checkSampleCode(documentationFile: File, sourceFile: File, tag: String) {
    val documentation = FileRange(documentationFile, tag)
    val source = FileRange(sourceFile, tag)

    fun List<String>.strippedLines(): List<String> =
        map { it.trim() }.filter { it.isNotEmpty() }

    val documentationLines = documentation.lines().strippedLines()
    if (documentationLines.isNotEmpty() && documentationLines != source.lines().strippedLines())
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
fun insertSamplesCode(parent: File, content: String): String {
    val samples = "@code (.*)".toRegex().findAll(content)
    var result = content

    samples.forEach { sample ->
        try {
            val sampleLocation = sample.groups[1]?.value?.trim() ?: error("Location expected")
            val url = URL("file:${parent.absolutePath}/$sampleLocation")
            val tag = url.query
            val lines = url.readText().lines()
            val start = lines.indexOfFirst { it.contains("// $tag") } + 1
            val end = lines.indexOfLast { it.contains("// $tag") } - 1
            val text = lines.slice(start..end).joinToString("\n").trimIndent()
            result = result.replace("@code $sampleLocation", "```kotlin\n$text\n```")
        }
        catch(e: Exception) {
            val code = sample.value
            println("ERROR: Unable to process '$code' in folder: '${parent.absolutePath}'")
            e.printStackTrace()
        }
    }

    return result
}

/**
 * Return a text with the proper syntax for the tabbed code blocks feature.
 *
 * @param content Text with incorrect format.
 * @return The content with the tabbed code blocks fixed.
 */
fun fixCodeTabs(content: String): String {
    val blocks = """=== "(.*)"\n\n```""".toRegex().findAll(content)
    var result = content

    blocks.forEach { block ->
        val tabName = block.groups[1]?.value?.trim() ?: error("Tab name expected")
        val replacement = "=== \"$tabName\"\n"
        result = result.replace("=== \"$tabName\"\n\n```", replacement)
    }

    return result.replace("    ```\n```", "    ```")
}
