
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

val compileTestKotlin: KotlinCompile by tasks

plugins {
    java
}

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
compileTestKotlin.dependsOn(tasks.getByPath(":port_http_server:compileTestKotlin"))

val entityTests: SourceSetOutput = project(":port_http_server").sourceSet("test").output
val entityTestsHexagonWeb: SourceSetOutput = project(":hexagon_web").sourceSet("test").output

extra["basePackage"] = "com.hexagonkt.http.server.jetty"

dependencies {
    val jettyVersion = properties["jettyVersion"]
    val logbackVersion = properties["logbackVersion"]

    "api"(project(":http_server_servlet"))
    "api"(project(":logging_slf4j"))
    "api"("org.eclipse.jetty:jetty-webapp:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty.http2:http2-server:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty:jetty-alpn-java-server:$jettyVersion") { exclude("org.slf4j") }

    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(entityTests)
    "testImplementation"(project(":hexagon_web"))
    "testImplementation"(entityTestsHexagonWeb)

    "testImplementation"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
}
