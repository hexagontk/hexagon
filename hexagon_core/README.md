
# Module hexagon_core

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Install the Dependency

This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging or dependency injection (i.e.: for
a desktop application), you can import it with the following code:

```groovy tab="build.gradle"
repositories {
    mavenCentral()
}

implementation("com.hexagonkt:hexagon_core:$hexagonVersion")
```

```xml tab="pom.xml"
<dependency>
  <groupId>com.hexagonkt</groupId>
  <artifactId>hexagon_core</artifactId>
  <version>$hexagonVersion</version>
</dependency>
```

### Logger

The following code block shows the most common use cases for the [Logger] class:

@sample hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:logger

Hexagon uses the [SLF4J] logging library, you can use any of its implementations by just adding the
library to your classpath. Below you can see some alternatives:

[SLF4J]: http://www.slf4j.org

```groovy tab="build.gradle"
    /*
     * Pick ONLY ONE of the options below
     */
    implementation("ch.qos.logback:logback-classic:1.2.3") // Full featured implementation
    implementation("org.slf4j:slf4j-simple:1.7.30") // Lightweight logging implementation
    implementation("org.slf4j:slf4j-jdk14:1.7.30") // Uses JDK logging API
```

```xml tab="pom.xml"
<!--
 ! Pick ONLY ONE of the options below
 !-->
<!-- Full featured implementation -->
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.2.3</version>
</dependency>
<!-- Lightweight logging implementation -->
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>1.7.30</version>
</dependency>
<!-- Uses JDK logging API -->
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-jdk14</artifactId>
  <version>1.7.30</version>
</dependency>
```

!!! Info
    You can bridge other logging libraries (that may be used by other third party libraries you use
    importing adaptor libraries (check [SLF4J bridge guide](http://www.slf4j.org/legacy.html) for
    details). For example:

```groovy tab="build.gradle"
    // Bridges
    runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.30")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.30")
    runtimeOnly("org.slf4j:jul-to-slf4j:1.7.30") // Don't add it if you are using 'slf4j-jdk14'
```

```xml tab="pom.xml"
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>jcl-over-slf4j</artifactId>
  <version>1.7.30</version>
</dependency>
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>log4j-over-slf4j</artifactId>
  <version>1.7.30</version>
</dependency>
<!-- Don't add the next one if you are using 'slf4j-jdk14' -->
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>jul-to-slf4j</artifactId>
  <version>1.7.30</version>
</dependency>
```

[Logger]: com.hexagonkt.helpers/-logger/index.md

### Dependency injection

You can take advantage of dependency injection using the [InjectionManager] object.

The implementation is a map of classes (with an optional tag) to provider functions (in essence:
`Map<KClass<*>, () -> Any>`). It is a very simple, yet complete, DI implementation.

You can bind supplier functions or objects to classes. If a class is already bound, later calls to
`bind*` methods are ignored. However, you can use the `forceBind*` methods if you need to override
a binding (in tests for example).

Check this sample to bind constructor functions or objects to classes, and inject them later:

@sample hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:injectionUsage

!!! Info
    Dependency Injection is not required by the toolkit. All classes and methods have versions
    receiving all of their dependencies, so you can use them instead relying on injection (or use
    another DI library of your choice).

[InjectionManager]: com.hexagonkt.injection/-injection-manager/index.md

### Serialization

The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

@sample hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:serializationUsage

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

# Package com.hexagonkt.helpers

JVM information, a logger class and other useful utilities.

# Package com.hexagonkt.injection

Utilities to bind classes to creation closures or instances, and inject instances of those classes
later.

# Package com.hexagonkt.serialization

Parse/serialize data in different formats to class instances.

# Package com.hexagonkt.settings

Load settings from different data sources and formats.

