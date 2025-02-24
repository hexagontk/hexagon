
rootProject.name = "hexagon"

include(
    "core",
    "handlers",
    "helpers",
    "site",
    "starters",
)

includeNestedModules(
    "http",
    "serialization",
    "serverless",
    "templates",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Build
            version("kotlin", "2.1.10")
            version("dokka", "2.0.0")
            version("licenseReport", "2.9")
            version("binValidator", "0.17.0")
            version("nativeTools", "0.10.5")
            version("jmhGradle", "0.7.2")
            version("gradleWrapper", "8.12.1")
            version("mkdocsMaterial", "9.6.4")
            version("maven", "3.9.9")
            version("jreleaser", "1.15.0")

            // Testing
            version("mockk", "1.13.16")
            version("jmh", "1.37")

            // Shared
            version("slf4j", "2.0.16")

            // http_server_netty
            version("netty", "4.1.118.Final")
            version("nettyTcNative", "2.0.70.Final")

            // http_server_helidon
            version("helidon", "4.1.6")

            // http_server_servlet
            version("servlet", "6.1.0")
            version("jetty", "12.0.16")

            // rest_tools
            version("swaggerRequestValidator", "2.44.1")

            // serialization
            version("jackson", "2.18.2")
            version("dslJson", "2.0.2")

            // serverless_http_google
            version("functions", "1.1.4")
            version("invoker", "1.3.3")

            // templates_freemarker
            version("freemarker", "2.3.34")

            // templates_jte
            version("jte", "3.1.16")

            // templates_pebble
            version("pebble", "3.2.3")

            // templates_rocker
            version("rocker", "2.2.1")
        }
    }
}

private fun includeNestedModules(vararg directories: String) {
    directories.forEach(::includeNestedModules)
}

private fun includeNestedModules(directory: String) {
    val dir = rootDir.resolve(directory)

    if (!dir.exists() || !dir.isDirectory)
        error("$directory directory must exist")

    include(":$directory")

    dir.listFiles()
        ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
        ?.filter { it.name != "browser" } // Included as build, not module (check above)
        ?.forEach {
            val name = it.name
            include(":$directory:$name")
            project(":$directory:$name").projectDir = it
        }
}
