
# Module port_templates

This port provides a common interface for rendering templates with multiple different template
engines.

### Install the Dependency
This module is not meant to be used directly. You should include any Adapter implementing this
feature (as [templates_pebble] and/or [templates_freemarker]) in order to process templates.

You can use many adapters in the same application to be able to handle different template engines at
the same time.

[templates_pebble]: /templates_pebble/
[templates_freemarker]: /templates_freemarker/

### Register a Template Engine
You can register multiple template engines with a regex:

@code port_templates/src/test/kotlin/TemplateManagerTest.kt:templateAdapterRegistration

Template is selected from top to bottom, picking the first matched one.

Special case for `.*` pattern

### Usage
To render a template, do something like this:

#### Templates are referenced by URLs
#### Special variables passed to templates
#### Locale usage

@code port_templates/src/test/kotlin/TemplateAdapterTest.kt:templateAdapterUsage

# Package com.hexagonkt.templates

Feature implementation code.
