
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.http.client"

dependencies {
    "api"(project(":http"))

    "testImplementation"(project(":serialization_yaml"))
    "testImplementation"(project(":http_server_jetty"))
}

extensions.configure<PublishingExtension> {
    (publications["mavenJava"] as MavenPublication).artifact(tasks.named("testJar"))
}
