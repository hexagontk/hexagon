
import org.apache.tools.ant.DirectoryScanner

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/service.gradle")
apply(from = "../gradle/junit.gradle")

plugins {
    application
    war
}

fun version(name: String): String =
    extra["${name}Version"]?.toString() ?: error("$name version not found")

val hikariVersion: String = version("hikari")
val postgresqlVersion: String = version("postgresql")
val logbackVersion: String = version("logback")
val jettyVersion: String = version("jetty")
val testngVersion: String = version("testng")

val tfbBuildDir: String = "${project.buildDir.absolutePath}/tfb"
val databases: List<String> = listOf("MongoDB", "PostgreSQL")
val servers: List<String> = listOf("Jetty", "Resin")

val tests: List<List<String>> = combine(servers, databases)

fun <T>combine(a: List<T>, b: List<T>): List<List<T>> =
    a.flatMap { aItem ->
        b.map { bItem ->
            listOf(aItem, bItem)
        }
    }

tasks.war {
    archiveFileName.set("ROOT.war")
}

tasks.installDist {
    dependsOn("war")
}

dependencies {
    implementation(project(":store_mongodb"))
    implementation(project(":http_server_jetty"))
    implementation(project(":templates_pebble"))

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    testImplementation(project(":http_client_ahc"))
}

tasks.register<Copy>("addGradlew") {
    from("$rootDir")
    include("gradle/", "gradlew", "gradlew.bat")
    exclude("gradle/*.gradle")
    into(tfbBuildDir)
}

tasks.register<Copy>("tfb") {
    dependsOn( "gradleSettings", "benchmarkConfig", "setupDockerfiles", "addGradlew")
    doFirst {
        DirectoryScanner.removeDefaultExclude("**/.gitignore")
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(projectDir)
    include("src/**")
    from("$projectDir/data")
    include("resin.xml")
    from("$projectDir/tfb")
    include(".gitignore", "build.gradle", "settings.gradle", "README.md")
    into(tfbBuildDir)

    doLast {
        DirectoryScanner.resetDefaultExcludes()
    }
}

val applicationClassName =
    project.extra["applicationClassName"]?.toString() ?: error("applicationClassName not found")

tasks.register<WriteProperties>("gradleSettings") {
    val repositoryFilesPath = "https://raw.githubusercontent.com/hexagonkt/hexagon"

    setProperties(
        mapOf(
            "name" to "hexagon",
            "description" to "Hexagon web framework's benchmark",

            "gradleScripts" to "$repositoryFilesPath/${rootProject.version}/gradle",
            "applicationClassName" to applicationClassName,

            "hexagonVersion" to rootProject.version.toString(),
            "logbackVersion" to logbackVersion,

            "hikariVersion" to hikariVersion,
            "postgresqlVersion" to postgresqlVersion,
            "jettyVersion" to jettyVersion,

            "testngVersion" to testngVersion
        )
    )

    outputFile = project.file("$tfbBuildDir/gradle.properties")
}

task("benchmarkConfig") {
    doLast {
        val testsMap: MutableMap<String, Any> = LinkedHashMap()

        tests.forEach {
            val server = it[0]
            val database = it[1]
            val databaseEngine = database.toLowerCase()
            val defaultCase = (server == servers.first() && database == databases.first())
            val name = if (defaultCase) "default" else "${server}-${database}"
            val port = if (server == "Resin") 8080 else 9090

            testsMap[name.toLowerCase()] = mapOf(
                "json_url" to "/json",
                "db_url" to "/$databaseEngine/db",
                "query_url" to "/$databaseEngine/query?queries=",
                "fortune_url" to "/$databaseEngine/pebble/fortunes",
                "update_url" to "/$databaseEngine/update?queries=",
                "plaintext_url" to "/plaintext",
                "port" to port,
                "approach" to "Realistic",
                "classification" to "Micro",
                "database" to if (database == "PostgreSQL") "postgres" else databaseEngine,
                "framework" to "Hexagon",
                "language" to "Kotlin",
                "orm" to "Raw",
                "platform" to "Servlet",
                "webserver" to "None",
                "os" to "Linux",
                "database_os" to "Linux",
                "display_name" to "Hexagon $server $database",
                "notes" to "http://hexagonkt.com",
                "versus" to "servlet"
            )
        }

        val config: Map<String, Any> = mapOf(
            "framework" to "hexagon",
            "tests" to listOf(testsMap)
        )

        // noinspection UnnecessaryQualifiedReference
        val json = groovy.json.JsonOutput.toJson(config)
        val file = file("$tfbBuildDir/benchmark_config.json")

        mkdir(tfbBuildDir)
        file.createNewFile()

        // noinspection UnnecessaryQualifiedReference
        file.writeText(groovy.json.JsonOutput.prettyPrint(json) + "\n")
    }
}

task("setupDockerfiles") {
    doLast {
        tests.forEach { row ->
            val server = row[0].toLowerCase()
            val database = row[1].toLowerCase()
            val defaultCase = row[0] == servers.first() && row[1] == databases.first()
            val name = if (defaultCase) "hexagon" else "hexagon-${server}-${database}"
            val file = File("$tfbBuildDir/${name}.dockerfile")
            val gradleImage = "6.3-jdk11"
            val jdkImage = "11-jre"

            val dockerfileBuild = """
                FROM gradle:$gradleImage AS gradle_build
                USER root
                WORKDIR /hexagon

                COPY src src
                COPY build.gradle build.gradle
                COPY gradle.properties gradle.properties
                RUN gradle --quiet --exclude-task test
            """

            val resinDockerfileRuntime = """
                FROM openjdk:$jdkImage
                ENV DBSTORE $database
                ENV ${database.toUpperCase()}_DB_HOST tfb-database
                ENV RESIN http://caucho.com/download/resin-4.0.64.tar.gz

                WORKDIR /resin
                RUN curl -sL ${'$'}RESIN | tar xz --strip-components=1
                RUN rm -rf webapps/*
                COPY --from=gradle_build /hexagon/build/libs/ROOT.war webapps/ROOT.war
                COPY resin.xml conf/resin.xml
                CMD ["java", "-jar", "lib/resin.jar", "console"]
            """

            val defaultDockerfileRuntime = """
                FROM openjdk:$jdkImage
                ENV DBSTORE $database
                ENV ${database.toUpperCase()}_DB_HOST tfb-database
                ENV WEBENGINE $server
                ENV PROJECT hexagon

                COPY --from=gradle_build /hexagon/build/install/\${'$'}PROJECT /opt/\${'$'}PROJECT
                ENTRYPOINT /opt/\${'$'}PROJECT/bin/\${'$'}PROJECT
            """

            val dockerfileRuntime =
                if (server == "resin") resinDockerfileRuntime else defaultDockerfileRuntime

            file.createNewFile()
            file.writeText("""
                #
                # BUILD
                #
                ${dockerfileBuild.trim()}

                #
                # RUNTIME
                #
                ${dockerfileRuntime.trim()}
            """.trimIndent())
        }
    }
}
