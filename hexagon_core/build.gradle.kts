
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"("org.slf4j:slf4j-api:${properties.get("slf4jVersion")}")
    "api"("org.slf4j:jcl-over-slf4j:${properties.get("slf4jVersion")}") { exclude(module = "slf4j-api") }
    "api"("org.slf4j:jul-to-slf4j:${properties.get("slf4jVersion")}") { exclude(module = "slf4j-api") }
    "api"("ch.qos.logback:logback-classic:${properties.get("logbackVersion")}") { exclude(module = "slf4j-api") }

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-properties:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:${properties.get("jacksonVersion")}")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:${properties.get("jacksonVersion")}") {
        exclude(group = "org.jetbrains.kotlin")
    }
}
