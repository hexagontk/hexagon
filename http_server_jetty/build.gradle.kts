
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(tasks.getByPath(":http_server:compileTestKotlin"))
val httpServerTest: SourceSetOutput = project(":http_server").sourceSet("test").output

extra["basePackage"] = "com.hexagonkt.http.server.jetty"

dependencies {
    val jettyVersion = properties["jettyVersion"]
    val logbackVersion = properties["logbackVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http_server_servlet"))
    "api"("org.slf4j:slf4j-api:$slf4jVersion")
    "api"("org.eclipse.jetty:jetty-webapp:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty.http2:http2-server:$jettyVersion") { exclude("org.slf4j") }
    "api"("org.eclipse.jetty:jetty-alpn-java-server:$jettyVersion") { exclude("org.slf4j") }

    "testImplementation"(httpServerTest)
    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(project(":web"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
}
