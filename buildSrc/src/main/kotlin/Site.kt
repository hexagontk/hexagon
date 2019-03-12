
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FileRange(private val file: File, private val range: IntRange) {

    companion object {
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

    fun lines(): List<String> = file.readLines().slice(range)

    fun text(): String = lines().joinToString("\n").trimIndent()

    fun strippedLines(): List<String> = lines().map { it.trim() }.filter { it.isNotEmpty() }

    override fun toString(): String = "$file.absolutePath [$range]"
}

fun loadYaml(yamlFile: File): Map<*, *> =
    ObjectMapper(YAMLFactory()).readValue(yamlFile, Map::class.java)

fun checkDocumentationCode(documentation: FileRange, source: FileRange) {
    if (documentation.strippedLines() != source.strippedLines())
        error("""
            Documentation $documentation does not match $source

            DOC -----------------------------------------------
            ${documentation.text()}

            SRC -----------------------------------------------
            ${source.text()}
        """.trimIndent())
}

fun addFrontMatter(markdownFile: File, content: String): String =
    """
        title=${markdownFile.name.replace("_", " ").capitalize().removeSuffix(".md")}
        date=${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}
        fileName=${markdownFile.name}
        type=page
        status=published
        ~~~~~~
    """.trimIndent() + "\n" + content

fun fixLinks(content: String): String {
    val title = "[ ._a-zA-Z0-9\\-]"
    val link = "[/._a-zA-Z0-9\\-]"
    return content.replace("""\[($title*)]\(($link*)\.md\)""".toRegex(), "[$1]($2.html)")
}

fun insertSamplesCode(markdownFile: File, content: String): String {
    val samples = "@sample (.*)".toRegex().findAll(content)
    var result = content

    samples.forEach { sample ->
        val sampleLocation = sample.groups[1]?.value?.trim() ?: error("Location expected")
        val fileRange = FileRange.parse(markdownFile, sampleLocation)
        val replacement = "```kotlin\n" + fileRange.text().trim() + "\n```"
        result = result.replace("@sample $sampleLocation", replacement)
    }

    return result
}
