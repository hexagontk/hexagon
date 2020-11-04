
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

dependencies {
    val jacksonVersion = "2.11.2"
    val junitVersion = "5.7.0"

    "implementation"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    "testImplementation"(gradleTestKit())
    "testImplementation"("org.junit.jupiter:junit-jupiter:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}
