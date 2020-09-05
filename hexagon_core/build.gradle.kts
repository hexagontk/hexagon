
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon core utilities. Includes DI, serialization, http and settings helpers."

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]
    val jacksonVersion = properties["jacksonVersion"]

    "api"("org.slf4j:slf4j-api:$slf4jVersion")

    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    "testRuntimeOnly"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testRuntimeOnly"("org.slf4j:jul-to-slf4j:$slf4jVersion")
    "testImplementation"(project(":serialization_yaml"))
    "testImplementation"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
}
