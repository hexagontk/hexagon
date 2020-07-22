
# Concepts

### Port

It is an interface for a task. The toolkit's ports are designed to work on their own. For example:
you can use the `http_server` module without importing the `templates` one, and the other way around
(taking only the dependencies you need for your application).

### Adapter

They are implementations of a functionality (Port) for a given product/technology. Clients should
only use ports' code (not Adapters specific code), this makes easy to switch among different
adapters with minimum impact.

Adapters are independent of each other, but you can use several adapters for the same port in a
single application.

### Manager

Singleton object to manage a cross toolkit aspect. I.e.: Serialization, Injection or Settings.

# Toolkit Structure

The project is composed by modules, each module provide a single functionality. There are three
kinds of modules:

* The ones that provide a functionality that does not depend on different implementations, like
  [hexagon_scheduler] or [hexagon_core]. Their name always start with the `hexagon_` prefix. These
  modules can depend on several Ports, but never on Adapters (see below).
* Modules that define a "Port": these are interfaces to a feature that may have different
  implementations (ie: [port_http_server] or [port_store]). They cannot be used by themselves and in
  their place, an adapter implementing them should be added to the list of dependencies. These
  modules' names start with the `port_` prefix. Ports are independent of each other.
* Adapter modules, which are Port implementations for a given tool, [store_mongodb] and
  [messaging_rabbitmq] are examples of this type of modules. Adapter names must start with their
  port name.

[hexagon_scheduler]: /hexagon_scheduler/index.html
[hexagon_core]: /hexagon_core/index.html

[port_http_server]: /port_http_server/index.html
[port_store]: /port_store/index.html

[store_mongodb]: /store_mongodb/index.html
[messaging_rabbitmq]: /messaging_rabbitmq/index.html

# Hexagon Core

The [Hexagon Core] module is used by all other libraries, so it would be added to your project
anyway just by using any adapter.

Core utilities like settings handling, logging, serialization and dependency injection.
The toolkit's ports are designed to use core functionalities. You can use a third party DI library
instead using the Core one. It depends on Logback and Jackson.

The main features it has are:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Dependency Injection]: bind classes to creation closures or instances and inject them.
* [Instance Serialization]: parse/serialize data in different formats to class instances.
* [Configuration Settings]: load settings from different data sources and formats.

[Hexagon Core]: /hexagon_core/index.html

[Helpers]: /hexagon_core/com.hexagonkt.helpers
[Dependency Injection]: /hexagon_core/com.hexagonkt.injection
[Instance Serialization]: /hexagon_core/com.hexagonkt.serialization
[Configuration Settings]: /hexagon_core/com.hexagonkt.settings

# Other Modules

The following libraries provide extra features not bound to different implementations. They will not
use dependencies outside the Hexagon toolkit.

* [Scheduling]: this module allows services to execute tasks periodically using Cron expressions.
  However, you have to be careful to not run tasks twice if you have many instances.
* [Web]: this module is meant to ease web applications development. Provides helpers for
  generating HTML and depends on the [HTTP Server] and [Templates] ports.

[Scheduling]: /hexagon_scheduler/index.html
[Web]: /hexagon_web/index.html

# Toolkit Ports

These modules define features that need an specific implementation. You can use many implementations
of each port at the same time. You can even provide a custom implementations if you want to optimize
a particular use case.

These are the implemented ports:

* [HTTP Server]: describes how to use HTTP routing and HTML templates for Web services.
* [HTTP Client]: documentation to use the HTTP client module to connect to other services.
* [Storage]: gives an overview of how to store data using different data stores.
* [Messaging]: how to support asynchronous communication with messages through message brokers.
* [Templates]: describes how to render pages using template engines.

[HTTP Server]: /port_http_server/index.html
[HTTP Client]: /port_http_client/index.html
[Storage]: /port_store/index.html
[Messaging]: /port_messaging/index.html
[Templates]: /port_templates/index.html
