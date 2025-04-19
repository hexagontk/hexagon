
# Module injection
Basic dependency injection support.

### Install Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk.extra:injection:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.extra</groupId>
      <artifactId>injection</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.injection
Utilities to bind classes to creation closures or instances, and inject instances of those classes
later.
