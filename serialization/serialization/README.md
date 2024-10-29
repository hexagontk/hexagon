
# Module serialization
This module holds serialization utilities.

### Install the Dependency
This module is not meant to be used directly. You should include an Adapter implementing this
feature (as [serialization_dsl_json]) in order to parse/serialize data.

[serialization_dsl_json]: serialization_dsl_json.md

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:serialization:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>serialization</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Defined Ports
TODO Document module exposed ports:
* SerializationFormat

### Serialization
The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

@code serialization/serialization_test/src/main/kotlin/com/hexagontk/serialization/test/SerializationTest.kt?serializationUsage

# Package com.hexagontk.serialization
Parse/serialize data in different formats to class instances.
