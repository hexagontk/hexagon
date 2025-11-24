
rootProject.name = "hexagon"

include(
    "core",
    "handlers",
    "helpers",
    "site",
    "starters",
)

includeNestedModules(
    "extra",
    "http",
    "messaging",
    "serialization",
    "serverless",
    "store",
    "templates",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Build
            version("kotlin", "2.3.0-RC")
            version("dokka", "2.0.0")
            version("licenseReport", "3.0.1")
            version("binValidator", "0.18.1")
            version("nativeTools", "0.11.3")
            version("jmhGradle", "0.7.3")
            version("gradleWrapper", "9.2.1")
            version("mkdocsMaterial", "9.7.0")
            version("maven", "4.0.0-rc-5")
            version("junit", "6.0.1")
            version("jreleaser", "1.20.0")

            // Testing
            version("mockk", "1.14.6")
            version("jacoco", "0.8.14")
            version("jmh", "1.37")
            version("testcontainers", "2.0.2")
            version("commonsCompress", "1.28.0")

            // Shared
            version("slf4j", "2.0.17")

            // http_server_netty
            version("netty", "4.2.7.Final")
            version("nettyTcNative", "2.0.74.Final")

            // http_server_helidon
            version("helidon", "4.3.2")

            // http_server_servlet
            version("servlet", "6.1.0")
            version("jetty", "12.1.4")

            // rest_tools
            version("swaggerRequestValidator", "2.46.0")

            // messaging_rabbitmq
            version("amqpClient", "5.27.0")
            version("metricsJmx", "4.2.37")

            // serialization
            version("jackson", "3.0.2")
            version("dslJson", "2.0.2")

            // serverless_http_google
            version("functions", "1.1.4")
            version("invoker", "1.4.1")

            // store_mongodb
            version("mongodb", "5.6.1")

            // templates_freemarker
            version("freemarker", "2.3.34")

            // templates_jte
            version("jte", "3.2.1")

            // templates_pebble
            version("pebble", "4.0.0")

            // templates_rocker
            version("rocker", "2.2.1")

            // scheduler
            version("cronutils", "9.2.1")
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
