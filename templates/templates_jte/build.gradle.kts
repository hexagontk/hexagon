import gg.jte.ContentType.Html

plugins {
    id("java-library")
    id("gg.jte.gradle") version("3.1.12")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Template processor adapter for 'jte'."

dependencies {
    val jteVersion = libs.versions.jte.get()

    "api"(project(":templates:templates"))
    "api"("gg.jte:jte:$jteVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))

    "jteGenerate"("gg.jte:jte-native-resources:$jteVersion")
}

tasks.named("compileKotlin") { dependsOn("generateJte") }
tasks.named("processResources") { dependsOn("processTestResources") }
// TODO Use flags to optimize development builds
//if (findProperty("enableDetekt") != null)
tasks.named("detektMain") { dependsOn("compileTestKotlin") }
tasks.named("sourcesJar") { dependsOn("compileTestKotlin") }

// TODO Remove when settings prevent this directory from being created (check .gitignore also)
tasks.named<Delete>("clean") {
    delete("jte-classes")
}

jte {
    sourceDirectory.set(projectDir.resolve("src/test/resources/templates").toPath())
    targetDirectory.set(projectDir.resolve("build/classes/kotlin/test").toPath())
    contentType.set(Html)

    jteExtension("gg.jte.nativeimage.NativeResourcesExtension")

    generate()
}
