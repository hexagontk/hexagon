
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

class FileRange(private val file: File, private val range: IntRange) {

//    constructor(parent: File, path: String) : this(parent.toPath().resolve(path).toFile())

    @JvmOverloads
    constructor(file: File, begin: Int? = null, end: Int? = null) : this(
        file,
        (begin ?: 0) .. (end ?: file.readLines().size - 1)
    )

    constructor(file: File, tag: String) : this(
        file,
        file.readLines().let { (it.indexOf("// $tag") + 1) .. (it.indexOf("/// $tag")) }
    )

    fun lines(): List<String> = file.readLines().slice(range)

    fun strippedLines(): List<String> = lines().map { it.trim() }.filter { it.isNotEmpty() }

    override fun toString(): String = "$file.absolutePath [$range]"
}

fun loadYaml(yamlFile: File): Map<*, *> =
    ObjectMapper(YAMLFactory()).readValue(yamlFile, Map::class.java)

fun checkDocumentationCode(documentation: FileRange, source: FileRange) {
    println(documentation.lines().joinToString("\n"))
    println(source.lines().joinToString("\n"))
    if (documentation.strippedLines() != source.strippedLines())
        error("Documentation $documentation does not match $source")
}
