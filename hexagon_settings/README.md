
# Module hexagon_settings

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Install the Dependency

This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging or dependency injection (i.e.: for
a desktop application), you can import it with the following code:

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

1. Resource `/application.yml`.
2. Environment variables starting with `APPLICATION_`.
3. System properties starting with `service`.
4. File `./application.yml` from the application run directory.
5. Resource `/application_test.yml`.

Below there is a code fragment showing how to add a custom settings source and load its properties:

@sample hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:settingsUsage

# Package com.hexagonkt.settings

Load settings from different data sources and formats.

