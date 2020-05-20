
import org.gradle.api.Project

/**
 * Returns the list of absolute paths of files matching the given pattern in the passed directory.
 *
 * @param directory Directory to look for files matching the pattern (relative to project's * path).
 * @param pattern Pattern to filter directory's files.
 * @return List of absolute paths of files matching the given pattern.
 * @receiver Project which path is used to resolve the passed directory.
 */
fun Project.filesCollection(directory: Any, pattern: String): List<String> =
    fileTree(directory) { include(pattern) }.files.map { it.absolutePath }

fun <T, R : Any> Iterable<T>.mapNotException(transform: (T) -> R?): List<R> = this.mapNotNull {
    try {
        transform(it)
    }
    catch (e: Exception) {
        null
    }
}

fun Project.modulesPaths(path: String) = subprojects
    .map { sp -> rootProject.file(sp.name + path) }
    .filter { it .exists() }
    .toTypedArray()
