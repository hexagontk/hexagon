
rootProject.name = "hexagon"

include(
    "core",
    "handlers",
    "site",
    "starters",
)

includeNestedModules(
    "http",
    "serialization",
    "templates"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Build
            version("kotlin", "2.2.0")
            version("dokka", "1.9.20")
            version("licenseReport", "2.9")
            version("binValidator", "0.18.0")
            version("nativeTools", "0.10.6")
            version("jmhGradle", "0.7.3")
            version("gradleWrapper", "8.14.2")
            version("mkdocsMaterial", "9.6.15")
            version("mermaidDokka", "0.6.0")
            version("maven", "3.9.10")

            // Testing
            version("mockk", "1.14.4")
            version("gatling", "3.10.5")
            version("jmh", "1.37")

            // Shared
            version("slf4j", "2.0.17")

            // http_server_netty
            version("netty", "4.2.2.Final")
            version("nettyTcNative", "2.0.72.Final")

            // http_server_helidon
            version("helidon", "4.2.3")

            // http_server_servlet
            version("servlet", "6.1.0")
            version("jetty", "12.0.23")

            // rest_tools
            version("swaggerRequestValidator", "2.44.9")

            // serialization
            version("jackson", "2.19.1")
            version("dslJson", "2.0.2")

            // templates_freemarker
            version("freemarker", "2.3.34")

            // templates_jte
            version("jte", "3.2.1")

            // templates_pebble
            version("pebble", "3.2.4")

            // templates_rocker
            version("rocker", "2.2.1")
        }
    }
}

fun includeNestedModules(vararg directories: String) {
    directories.forEach(::includeNestedModules)
}

fun includeNestedModules(directory: String) {
    val dir = rootDir.resolve(directory)

    if (!dir.exists() || !dir.isDirectory)
        error("$directory directory must exist")

    include(":$directory")

    dir.listFiles()
        ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
        ?.forEach {
            val name = it.name
            include(":$directory:$name")
            project(":$directory:$name").projectDir = it
        }
}
