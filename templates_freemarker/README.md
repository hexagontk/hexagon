
# Module templates_freemarker

This module provides an adapter for the templates Port supporting the Apache [FreeMarker] template
engine.

[FreeMarker]: https://freemarker.apache.org

### Install the Dependency
```groovy tab="build.gradle"
repositories {
    mavenCentral()
}

implementation("com.hexagonkt:templates_freemarker:$hexagonVersion")
```

```xml tab="pom.xml"
<dependency>
  <groupId>com.hexagonkt</groupId>
  <artifactId>templates_freemarker</artifactId>
  <version>$hexagonVersion</version>
</dependency>
```

### How to Use

```
val contextVariables = hashMapOf(
    "contextVariableKey" to contextVariableValue
)
val renderedPage = FreeMarkerAdapter.render(
    "templates/page.html",
    Locale.getDefault(),
    contextVariables
)
```

If you have no context variables, you can simply pass an empty map:

```
val renderedPage = FreeMarkerAdapter.render(
    "templates/page.html",
    Locale.getDefault(),
    hashMapOf<String, Any>()
)
```

# Package com.hexagonkt.templates.freemarker

Classes defined in this package implement the Templates Port interface with the [FreeMarker]
implementation.
