
import org.gradle.api.Project
import java.io.File

/**
 * Returns the set of files matching the given pattern in the passed directory.
 *
 * @param directory Directory to look for files matching the pattern (relative to project's * path).
 * @param include Pattern to filter directory's files.
 * @return Set of files matching the given pattern.
 * @receiver Project which path is used to resolve the passed directory.
 */
fun Project.filesCollection(directory: Any = this.projectDir, include: String): Set<File> =
    fileTree(directory) { this.include(include) }.files

/**
 * Returns the list of absolute paths of files matching the given pattern in the passed directory.
 *
 * @param directory Directory to look for files matching the pattern (relative to project's * path).
 * @param include Pattern to filter directory's files.
 * @return List of absolute paths of files matching the given pattern.
 * @receiver Project which path is used to resolve the passed directory.
 */
fun Project.pathsCollection(directory: Any = this.projectDir, include: String): List<String> =
    filesCollection(directory, include).map { it.absolutePath }

fun Project.modulesPaths(path: String): List<File> =
    subprojects.map { rootProject.file("${it.name}/$path") }.filter { it .exists() }
