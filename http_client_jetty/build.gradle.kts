
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http_client"))
    "api"(platform("org.eclipse.jetty:jetty-bom:$jettyVersion"))
    "api"("org.eclipse.jetty:jetty-client") {
        exclude(group = "org.slf4j")
    }

    "testImplementation"("org.eclipse.jetty.websocket:websocket-jetty-client")
}
