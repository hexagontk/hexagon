
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
            version("kotlin", "2.0.10")
            version("dokka", "1.9.20")
            version("licenseReport", "2.8")
            version("binValidator", "0.16.3")
            version("nativeTools", "0.10.2")
            version("detekt", "1.23.6")
            version("jmhGradle", "0.7.2")
            version("gradleWrapper", "8.9")
            version("mkdocsMaterial", "9.5.31")
            version("mermaidDokka", "0.6.0")
            version("maven", "3.9.8")

            // Testing
            version("junit", "5.10.3")
            version("mockk", "1.13.12")
            // TODO Latest version breaks stress test (consider using JMeter)
            //version("gatling", "3.11.3")
            version("gatling", "3.10.5")
            version("jmh", "1.37")

            // Shared
            version("slf4j", "2.0.13")

            // http_server_netty
            version("netty", "4.1.112.Final")
            version("nettyTcNative", "2.0.65.Final")

            // http_server_helidon
            version("helidon", "4.0.11")

            // http_server_servlet
            version("servlet", "6.0.0")
            version("jetty", "12.0.12")

            // rest_tools
            version("swaggerRequestValidator", "2.41.0")

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
