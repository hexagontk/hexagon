
# [FreeMarker] template engine adapter for Hexagon.

Module: templates_freemarker

Package: com.hexagonkt.templates.freemarker

[FreeMarker]: https://freemarker.apache.org

### How to Use

TODO Replace with @sample to test usage

TODO Add the package description at this file's end

TODO Review documentation in site

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

If you have no context variables, you can simply pass an empty HashMap

```
val renderedPage = FreeMarkerAdapter.render(
    "templates/page.html",
    Locale.getDefault(),
    hashMapOf<String, Any>()
)
```
