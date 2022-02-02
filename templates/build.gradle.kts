
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.templates"

dependencies {
    "api"(project(":core"))

    "testImplementation"(project(":serialization_jackson_yaml"))
}

extensions.configure<PublishingExtension> {
    (publications["mavenJava"] as MavenPublication).artifact(tasks.named("testJar"))
}
