
# Module hexagon_settings

This module holds utilities to handle applications' configuration parameters.

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:hexagon_settings:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>hexagon_settings</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Settings

This module helps loading external settings from different sources. You can change the settings
sources, the default ones are (bottom sources override top ones):

1. Resource `/application.json`.
2. Resource `/application.yml`.
3. Environment variables starting with `APPLICATION_`.
4. System properties starting with `service`.
5. File `./application.json` from the application run directory.
6. File `./application.yml` from the application run directory.
7. Resource `/application_test.json`.
8. Resource `/application_test.yml`.

Configuration parameters are loaded from the defined sources into a map. Latter sources override
previous parameters with the same name if they exist.

!!! Tip
    The map with configuration parameters can be converted to any type using the [toObject]
    utility method in [Core serialization helpers].

Below there is a code fragment showing how to add custom settings sources and load its properties:

@code hexagon_settings/src/test/kotlin/HexagonSettingsSamplesTest.kt:settingsUsage

For defining simple settings, data classes can be used instead this module:

@code hexagon_settings/src/test/kotlin/HexagonSettingsSamplesTest.kt:settingsDataClasses

[toObject]: /api/hexagon_core/hexagon_core/com.hexagonkt.serialization/to-object.html
[Core serialization helpers]: /api/hexagon_core/hexagon_core/com.hexagonkt.serialization

# Package com.hexagonkt.settings

Load settings from different data sources and formats.

