
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
    The map with configuration parameters can be converted to any type using the `convertToObject`
    utility method in Core serialization helpers. TODO Add links to the methods/modules.

TODO Add example of defining simple settings using data classes instead this module.

Below there is a code fragment showing how to add custom settings sources and load its properties:

@code hexagon_settings/src/test/kotlin/HexagonSettingsSamplesTest.kt:settingsUsage

# Package com.hexagonkt.settings

Load settings from different data sources and formats.

