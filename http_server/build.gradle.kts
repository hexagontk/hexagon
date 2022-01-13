
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.http.server"

dependencies {
    val kotlinxCoroutinesVersion = properties["kotlinxCoroutinesVersion"]

    "api"(project(":http"))
    "testImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
}
