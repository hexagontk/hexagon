
# Module serialization
This module holds serialization utilities.

### Install the Dependency
This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging or serialization (i.e., for a
desktop application), you can import it with the following code:

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:serialization:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>serialization</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Defined Ports
TODO Document module exposed ports:
* Mapper
* SerializationFormat

### Serialization
The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

@code core/src/test/kotlin/HexagonCoreSamplesTest.kt:serializationUsage

# Package com.hexagonkt.serialization
Parse/serialize data in different formats to class instances.
