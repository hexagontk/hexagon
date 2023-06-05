
rootProject.name = "hexagon"

include(
    "core",
    "handlers",
    "handlers_async",
    "site",
    "starters",
)

includeModules(
    "http",
    "logging",
    "serialization",
    "templates"
)

fun includeModules(vararg directories: String) {
    directories.forEach(::includeModules)
}

fun includeModules(directory: String) {
    val dir = File(directory)

    if (!dir.exists() || !dir.isDirectory)
        error("$directory directory must exist")

    include(":$directory")

    dir.listFiles()
        ?.filter { it.isDirectory }
        ?.filter { it.resolve("build.gradle.kts").isFile }
        ?.forEach {
            val name = it.name
            include(":$directory:$name")
            project(":$directory:$name").projectDir = it
        }
}
