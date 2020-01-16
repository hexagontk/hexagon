
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"(project(":hexagon_core"))
    "api"("org.asynchttpclient:async-http-client:${properties["ahcVersion"]}") {
        exclude(module = "slf4j-api")
    }

    "testImplementation"(project(":http_server_jetty"))
}
