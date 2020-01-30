
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

plugins {
    java
}

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
//compileTestKotlin.dependsOn(tasks.getByPath(":port_http_client:compileTestKotlin"))
//
//val entityTests: SourceSetOutput = project(":port_http_client").sourceSets["test"].output

dependencies {
    "api"(project(":port_http_client"))
    "api"("org.asynchttpclient:async-http-client:${properties["ahcVersion"]}") {
        exclude(module = "slf4j-api")
    }

    "testImplementation"(project(":http_server_jetty"))
//    "testImplementation"(entityTests)
}
