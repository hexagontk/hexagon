
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.store.mongodb"

dependencies {
    val mongodbVersion = properties["mongodbVersion"]
    val testcontainersVersion = properties["testcontainersVersion"]

    "api"(project(":port_store"))
    "api"("org.mongodb:mongodb-driver-sync:$mongodbVersion")

    "testImplementation"(project(":serialization_json"))
    "testImplementation"("org.testcontainers:mongodb:$testcontainersVersion")
}
