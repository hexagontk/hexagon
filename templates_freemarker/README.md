
# [Freemarker](https://freemarker.apache.org/) template engine adapter for Hexagon.

Module: templates_freemarker

Package: com.hexagonkt.templates.freemarker

### How to Use

```
val contextVariables = hashMapOf(
    "contextVariableKey" to contextVariableValue
)
val renderedPage = FreemarkerAdapter.render(
    "templates/page.html",
    Locale.getDefault(),
    contextVariables
)
```

If you have no context variables, you can simply pass an empty HashMap

```
val renderedPage = FreemarkerAdapter.render(
    "templates/page.html",
    Locale.getDefault(),
    hashMapOf<String, Any>()
)
```
