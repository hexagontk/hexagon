
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(tasks.getByPath(":port_http_client:compileTestKotlin"))
val httpClientTest: SourceSetOutput = project(":port_http_client").sourceSet("test").output

extra["basePackage"] = "com.hexagonkt.http.client.ahc"

dependencies {
    "api"(project(":port_http_client"))
    "api"("org.asynchttpclient:async-http-client:${properties["ahcVersion"]}") {
        exclude("org.slf4j")
    }

    "testImplementation"(project(":serialization_yaml"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(httpClientTest)
}
