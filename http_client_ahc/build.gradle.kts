
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

plugins {
    java
}

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
compileTestKotlin.dependsOn(tasks.getByPath(":port_http_client:compileTestKotlin"))

val entityTests: SourceSetOutput = project(":port_http_client").sourceSets["test"].output

dependencies {
    "api"(project(":port_http_client"))
    "api"("org.asynchttpclient:async-http-client:${properties["ahcVersion"]}") {
        exclude(module = "slf4j-api")
    }

    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(entityTests)
}
