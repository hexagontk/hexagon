
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

extensions.configure<KotlinDslPluginOptions> {
    experimentalWarning.set(false)
}

repositories {
    jcenter()
}

dependencies {
    val jacksonVersion = "2.12.1"
    val junitVersion = "5.7.1"

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
