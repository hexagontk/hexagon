
import org.gradle.api.Project
import java.io.File
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Return the source set with the given name.
 *
 * @param name Name of the searched source set.
 * @return Project's source set with the given name.
 * @receiver Project used to search the source set.
 */
fun Project.sourceSet(name: String): SourceSet =
    this.extensions.getByType(SourceSetContainer::class.java).getByName(name)

/**
 * Return the list of absolute paths of files matching the given pattern in the passed directory.
 *
 * @param directory Directory to look for files matching the pattern (relative to project's path).
 *   Defaults to the project directory.
 * @param include Pattern to filter directory's files. Uses Ant glob syntax.
 * @return List of absolute paths of files matching the given pattern.
 * @receiver Project which path is used to resolve the passed directory.
 */
fun Project.pathsCollection(directory: Any = this.projectDir, include: String): List<String> =
    fileTree(directory) { include(include) }
        .files
        .map { it.absolutePath }

/**
 * Get the list of files with the searched relative path in all modules that have them.
 *
 * @return List of files with the relative path searched in all modules (which have them).
 * @param path Path looked for inside each project's module.
 * @receiver Project used to gather submodules.
 */
fun Project.modulesPaths(path: String): List<File> =
    subprojects.map { rootProject.file("${it.name}/$path") }.filter { it .exists() }
