
# Module templates_rocker
[Rocker] template engine adapter for Hexagon.

For usage instructions, refer to the [Templates Port documentation](/templates/).

For using Rocker templates in GraalVM native images, the template file (and the classes) need to be
specified as resources in the configuration (I.e.: using `-H:IncludeResources=.*\\.(html|class)`).
Adding the classes used in the templates to the `reflect-config.json` file is also required.

[Rocker]: https://github.com/fizzed/rocker

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:templates_rocker:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>templates_rocker</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

## Use the Adapter
In order to use this adapter you need to set up a build plugin to compile the templates. To do so in
Gradle, add the following lines to `build.gradle.kts`:

```kotlin
rocker {
  configurations {
    create("main") {
      templateDir.set(file("src/main/resources"))
      optimize.set(true)
    }
  }
}
```

On top of that, you must also declare the template parameters this way:
`@args(java.util.Map<String, Object> context)` and use the data from the map.

# Package com.hexagonkt.templates.rocker
Classes that implement the Templates Port interface with the [Rocker] engine.
