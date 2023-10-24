
# Module templates
This port provides a common interface for rendering templates with multiple different template
engines.

## Install the Dependency
This module is not meant to be used directly. You should include any Adapter implementing this
feature (as [templates_pebble] and/or [templates_freemarker]) in order to process templates.

You can use many adapters in the same application to be able to handle different template engines at
the same time.

[templates_pebble]: /templates_pebble/
[templates_freemarker]: /templates_freemarker/

# Register a Template Engine
You can register multiple template engines using regular expressions:

@code templates/templates/src/test/kotlin/com/hexagonkt/templates/examples/TemplatesTest.kt?templateRegex

The template adapter is selected from top to bottom, picking the first matched one.

You can use Glob syntax to bind patterns to template adapters if you prefer:

@code templates/templates/src/test/kotlin/com/hexagonkt/templates/examples/TemplatesTest.kt?templateGlob

# Usage
To render a template, you have to use the [TemplateManager] object. The data to be used inside the
template must be supplied in a map (context), the template URL and current time-stamp (`_template_`
and `_now_` variables) are added to the context automatically. Check the code below for an example:

@code templates/templates/src/test/kotlin/com/hexagonkt/templates/examples/TemplatesTest.kt?templateUsage

[TemplateManager]: /api/templates/com.hexagonkt.templates/-template-manager/index.html

# Package com.hexagonkt.templates
Feature implementation code.
