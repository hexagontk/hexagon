
import java.io.File

import org.gradle.api.Project

/**
 * Represents a set of lines inside a text file.
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

    @JvmOverloads
    constructor(file: File, begin: Int? = null, end: Int? = null) : this(
        file,
        (begin ?: 0) .. (end ?: file.readLines().size - 1)
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

    fun strippedLines(): List<String> =
        lines().map { it.trim() }.filter { it.isNotEmpty() }

    override fun toString(): String =
        "$file.absolutePath [$range]"

    private fun lines(): List<String> =
        file.readLines().slice(range)
}

/**
 * Represent two file ranges from different files but with the same tag.
 *
 * @param source File for the first range.
 * @param target File for the second range.
 * @param tag Tag used for both file ranges.
 */
data class FilesRange(val source: File, val target: File, val tag: String)

/**
 * Assures that two file ranges are the same (to verify that documentation contains
 * only tested code).
 *
 * @param documentation File range for the documentation to check.
 * @param source File range for the source code that should be included in the documentation.
 */
fun checkSamplesCode(documentation: FileRange, source: FileRange) {
    if (documentation.strippedLines() != source.strippedLines())
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
 * Returns a text with samples replaced inside.
 *
 * @param parent Base directory used to resolve the files in the content samples.
 * @param content Text with `@sample` placeholders to be replaced by actual file ranges.
 * @return The content with the samples inserted in the placeholders.
 */
fun insertSamplesCode(parent: File, content: String): String {
    val samples = "@sample (.*)".toRegex().findAll(content)
    var result = content

    samples.forEach { sample ->
        val sampleLocation = sample.groups[1]?.value?.trim() ?: error("Location expected")
        val fileRange = FileRange.parse(parent, sampleLocation)
        val replacement = "```kotlin\n" + fileRange.text().trim() + "\n```"
        result = result.replace("@sample $sampleLocation", replacement)
    }

    return result
}

/**
 * Adds the correct `edit_url` to the site's Markdown pages.
 *
 * @param siteContentPath Base URL to process .md` files and add them the `edit_url` property.
 * @param project Gradle project used to look for `.md` files.
 */
fun addMetadata(siteContentPath: String, project: Project) {
    project.filesCollection(siteContentPath, "**/*.md").forEach { fileName ->
        val md = File(fileName)
        val mdText = md.readText()
        val mdPath = fileName.removePrefix("$siteContentPath/")
        if (mdPath != "index.md")
            md.writeText("edit_url: edit/develop/" + toEditUrl(mdPath) + "\n" + mdText)
    }
}

private fun toEditUrl(mdPath: String): String {
    val mdParts = mdPath.split(File.separator)

    return when {
        mdParts.size == 2 && mdParts[1].removeSuffix(".md") in listOf("index", mdParts[0]) ->
            "${mdParts[0]}/README.md"
        mdParts.size == 3 && mdParts[2] == "index.md" ->
            "${mdParts[0]}/README.md"
        mdParts.size >= 3 && mdParts[1].contains("com.hexagon") -> {
            val module = mdParts[0]
            val sourceType =
                if (mdParts[1].contains("test") || mdParts[2].endsWith("-test")) "test"
                else "main"
            val packagePath = mdParts[1].replace(".", "/")
            val className =
                mdParts[2]
                    .replace("-[a-z]".toRegex()) { it.value.toUpperCase().substring(1) }
                    .removeSuffix(".md")
            "$module/src/$sourceType/kotlin/$packagePath/${className}.kt"
        }
        else ->
            "hexagon_site/pages/$mdPath"
    }
}
