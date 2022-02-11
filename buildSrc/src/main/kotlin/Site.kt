
import java.io.File

/**
 * Set of lines inside a text file.
 *
 * @param file Text file holding the lines.
 * @param range Range of lines inside the supplied file.
 */
class FileRange(private val file: File, private val range: IntRange) {

    companion object {

        /**
         * Parse a file range from a description string and the directory used to resolve the file.
         *
         * @param parent Directory used to locate the file range file.
         * @param path String to create the FileRange. The format is: <file>:[<tag>|<from>,<to>].
         */
        fun parse(parent: File, path: String): FileRange {
            val tokens = path.split(":")
            val filePath = tokens[0].trim()
            val file = parent.toPath().resolve(filePath).toFile()
            return if (tokens.size == 1) {
                FileRange(file)
            }
            else {
                val range = tokens[1]
                val rangeTokens = range.split(",")
                if (rangeTokens.size == 1)
                    FileRange(file, rangeTokens[0])
                else
                    FileRange(file, rangeTokens[0].toInt(), rangeTokens[1].toInt())
            }
        }
    }

    constructor(file: File, begin: Int? = null, end: Int? = null) : this(
        file,
        (begin ?: 0)..(end ?: (file.readLines().size - 1))
    )

    constructor(file: File, tag: String) : this(
        file,
        file
            .readLines()
            .map { it.trim() }
            .let { lines ->
                val start = lines.indexOfFirst { it.contains("// $tag") } + 1
                val end = lines.indexOfLast { it.contains("// $tag") } - 1
                start .. end
            }
    )

    fun text(): String =
        lines().joinToString("\n").trimIndent()

    override fun toString(): String =
        "$file.absolutePath [$range]"

    fun lines(): List<String> =
        file.readLines().slice(range)
}

/**
 * Two file ranges from different files but with the same tag.
 *
 * @param source File for the first range.
 * @param target File for the second range.
 * @param tag Tag used for both file ranges.
 */
data class FilesRange(val source: File, val target: File, val tag: String)

/**
 * Assure that two file ranges are the same (to verify that documentation contains
 * only tested code).
 *
 * @param documentation File range for the documentation to check.
 * @param source File range for the source code that should be included in the documentation.
 */
fun checkSamplesCode(documentation: FileRange, source: FileRange) {

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
 * Check a list for FilesRange in one call.
 *
 * @param ranges Set of FilesRanges to be checked.
 */
fun checkSamplesCode(vararg ranges: FilesRange) {
    ranges.forEach {
        checkSamplesCode(FileRange(it.source, it.tag), FileRange(it.target, it.tag))
    }
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
            val fileRange = FileRange.parse(parent, sampleLocation)
            val replacement = "```kotlin\n" + fileRange.text().trim() + "\n```"
            result = result.replace("@code $sampleLocation", replacement)
        }
        catch(e: Exception) {
            val code = sample.value
            println("ERROR: Unable to process '$code' in folder: '${parent.absolutePath}'")
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
