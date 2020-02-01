
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")
apply(from = "../gradle/sonarqube.gradle")

dependencies {
    val mongodbVersion = properties["mongodbVersion"]

    "api"(project(":port_store"))
    "api"("org.mongodb:mongodb-driver:$mongodbVersion")
    "api"("org.mongodb:mongodb-driver-async:$mongodbVersion")
}
