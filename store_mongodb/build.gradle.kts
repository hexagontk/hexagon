
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"(project(":port_store"))
    "api"("org.mongodb:mongodb-driver:${properties.get("mongodbVersion")}")
    "api"("org.mongodb:mongodb-driver-async:${properties.get("mongodbVersion")}")
}
