
# Module handlers
Provide general utilities to attach many handlers to be applied on events processing. Events can be
of any type.

## Install the Dependency
This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging, etc. (i.e., for a desktop
application), you can import it with the following code:

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:handlers:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>handlers</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.handlers
Provide general utilities to attach many handlers to be applied on events processing. Events can be
of any type.

Handlers may or may not be applied to certain events at runtime based on rules provided as a
predicate (function).

If an event doesn't match a handler's predicate, that handler is ignored, and the next one will be
processed.

When a handler's predicate returns true, its callback will be called. That block will process the
event, and return a modified copy with the changes.

The callback block will decide if subsequent handler's will be evaluated by calling the next handler
explicitly (if `next` method is not called, the handlers defined later won't be evaluated).

There are types of handlers like 'After' y 'Before' that call `next` as part as their operation
discharging the user of doing so.

The events are passed to handlers' callbacks wrapped in a 'context'. The context can also pass
information among handlers in the `attributes` field. And store an exception if it is thrown in a
previous handler processing.

Concepts:

* Events
    * Context
* Handlers
    * Predicates
    * Callbacks

Types of handlers:

* On Handler: the callback is executed before calling the remaining handlers. `next` is called after
  the callback.
* After Handler: the predicate is evaluated after the remaining handlers return, and the callback is
  executed after. `next` is called before the callback
* Filter Handler: `next` is not called by this handler, its callback is responsible for doing so if
  needed.
* Chain Handler: this handler contains a list of handlers inside.

IMPORTANT: the order is NOT the order, it is the depth. Handlers are not linked, they are NESTED.
The `next()` method passes control to the next level.

This definition:

```
H1
H2
H3
```

Is really this execution:

```
H1 (on)
  H2 (on)
    H3 (on)
  H2 (after)
H1 (after)
```
