
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
    fileTree(directory) { it.include(pattern) }.files.map { it.absolutePath }
