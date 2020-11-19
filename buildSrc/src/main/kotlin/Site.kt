
import java.io.File

import org.gradle.api.Project

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
        val sampleLocation = sample.groups[1]?.value?.trim() ?: error("Location expected")
        val fileRange = FileRange.parse(parent, sampleLocation)
        val replacement = "```kotlin\n" + fileRange.text().trim() + "\n```"
        result = result.replace("@code $sampleLocation", replacement)
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

/**
 * Add the correct `edit_url` to the site's Markdown pages.
 *
 * @param siteContentPath Base URL to process .md` files and add them the `edit_url` property.
 * @receiver Gradle project used to look for `.md` files.
 */
fun Project.addMetadata(siteContentPath: String) {
    pathsCollection(siteContentPath, "**/*.md").forEach { fileName ->
        val md = File(fileName)
        val mdText = md.readText()
        val mdPath = fileName.removePrefix("$siteContentPath/")
        if (mdPath != "index.md")
            md.writeText("edit_url: edit/develop/" + toEditUrl(mdPath) + "\n" + mdText)
    }
}

private fun Project.toEditUrl(mdPath: String): String {
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
            val classFile =
                mdParts[2]
                    .replace("-[a-z]".toRegex()) { it.value.toUpperCase().substring(1) }
                    .removeSuffix(".md") + ".kt"
            val packagePath = classPackage(module, sourceType, classFile)
            "$module/src/$sourceType/kotlin/$packagePath$classFile"
        }
        else ->
            "hexagon_site/pages/$mdPath"
    }
}

private fun Project.classPackage(module: String, sourceType: String, classFile: String): String =
    fileTree("$module/src/$sourceType/kotlin") { include("**/$classFile") }
        .files
        .firstOrNull()
        ?.relativeTo(file("$module/src/$sourceType/kotlin"))
        ?.parentFile
        ?.path
        ?.let { "$it/" }
        ?: ""
