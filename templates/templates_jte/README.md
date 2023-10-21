
# Module templates_jte
[jte] template engine adapter for Hexagon.

For usage instructions, refer to the [Templates Port documentation](/templates/).

[jte]: https://jte.gg

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:templates_jte:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>templates_jte</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

## Use the Adapter
In order to use this adapter you need to set up a build plugin to compile the templates. To do so in
Gradle, add the following lines to `build.gradle.kts`:

```kotlin
plugins {
  id("gg.jte.gradle") version("3.1.3")
}

dependencies {
  "jteGenerate"("gg.jte:jte-native-resources:$jteVersion")
}

tasks.named("compileKotlin") { dependsOn("generateJte") }

jte {
  sourceDirectory.set(projectDir.resolve("src/main/resources/templates").toPath())
  contentType.set(gg.jte.ContentType.Html)

  jteExtension("gg.jte.nativeimage.NativeResourcesExtension")

  generate()
}
```

# TODO
* Don't create `jte-classes` directory
* Generate template classes only for tests
* Test file loaded templates
* Test plain test templates

# Package com.hexagonkt.templates.jte
Classes that implement the Templates Port interface with the [jte] engine.
