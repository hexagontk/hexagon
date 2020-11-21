
# Module port_templates

This port provides a common interface for rendering templates with multiple different template
engines.

### Install the Dependency
This module is not meant to be used directly. You should include any Adapter implementing this
feature (as [templates_pebble] and/or [templates_freemarker]) in order to process templates.

You can use many adapters in the same application to be able to handle different template engines at
the same time.

[templates_pebble]: /templates_pebble
[templates_freemarker]: /templates_freemarker

# Package com.hexagonkt.templates

### Create a Template Engine
You can create a template engine with default settings as follows:

@code port_templates/src/test/kotlin/TemplateEngineTest.kt:templateEngineCreation

### Settings
Template engines can be configured:

@code port_templates/src/test/kotlin/TemplateEngineTest.kt:templateEngineSettingsCreation

### Usage
To render a template,  do something like this:

@code port_templates/src/test/kotlin/TemplateEngineTest.kt:templateEngineUsage

### Using multiple template engines
To make the use of multiple template engines more convenient, you can use the TemplateManager.
Just register multiple template engines (or the same engine with different configurations) under a
prefix and use it like follows:

@code port_templates/src/test/kotlin/TemplateManagerTest.kt:templateEngineRegistration
