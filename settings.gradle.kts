
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
    "serverless",
    "templates"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Build
            version("kotlin", "2.0.20")
            version("dokka", "1.9.20")
            version("licenseReport", "2.9")
            version("binValidator", "0.16.3")
            version("nativeTools", "0.10.3")
            version("detekt", "1.23.7")
            version("jmhGradle", "0.7.2")
            version("gradleWrapper", "8.10.2")
            version("mkdocsMaterial", "9.5.37")
            version("mermaidDokka", "0.6.0")
            version("maven", "3.9.9")
            version("jreleaser", "1.14.0")

            // Testing
            version("junit", "5.11.1")
            version("mockk", "1.13.12")
            // TODO Replace with code using HTTP client and virtual threads
            version("gatling", "3.10.5")
            version("jmh", "1.37")

            // Shared
            version("slf4j", "2.0.16")

            // http_server_netty
            version("netty", "4.1.113.Final")
            version("nettyTcNative", "2.0.66.Final")

            // http_server_helidon
            version("helidon", "4.1.1")

            // http_server_servlet
            version("servlet", "6.1.0")
            version("jetty", "12.0.13")

            // rest_tools
            version("swaggerRequestValidator", "2.42.0")

            // serialization
            version("jackson", "2.17.2")
            version("dslJson", "2.0.2")

            // serverless_http_google
            version("functions", "1.1.0")
            version("invoker", "1.3.1")

            // templates_freemarker
            version("freemarker", "2.3.33")

            // templates_jte
            version("jte", "3.1.12")

            // templates_pebble
            version("pebble", "3.2.2")

            // templates_rocker
            version("rocker", "1.4.0")
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
