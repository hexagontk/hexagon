
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    val jacksonVersion = "2.12.3"
    val junitVersion = "5.7.2"

    "implementation"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    "testImplementation"(gradleTestKit())
    "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        if (logger.isInfoEnabled)
            events("skipped", "failed", "standardOut", "standardError")
        else
            events("skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}
