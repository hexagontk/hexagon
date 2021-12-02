
# Concepts

### Port

It is an interface for a task. The toolkit ports are designed to work on their own. For example: you
can use the `http_server` module without importing the `templates` one, and the other way around
(taking only the dependencies you need for your application).

### Adapter

They are implementations of a functionality (Port) for a given product/technology. Clients should
only use ports' code (not Adapters specific code), this makes it easy to switch among different
adapters with minimum impact.

Adapters are independent of each other, but you can use several adapters for the same port in a
single application.

### Manager

Singleton object to manage a cross toolkit aspect. I.e., Serialization, Logging or Templates.

# Toolkit Structure

The project is composed of modules, each module provides a single functionality. There are three
kinds of modules:

* The ones that provide functionality that does not depend on different implementations, like
  [core]. Their name always starts with the `hexagon_` prefix. These modules can depend on several
  Ports, but never on Adapters (see below).
* Modules that define one or more related "Ports": these are interfaces to a feature that may have
  different implementations (i.e., [http_server] or [templates]). They cannot be used by
  themselves and in their place, an adapter implementing them should be added to the list of
  dependencies. These modules' names start with the `port_` prefix. Ports are independent of each
  other.
* Adapter modules, which are Port implementations for a given tool, [http_client_ahc], and
  [http_server_jetty] are examples of this type of module. Adapter names must start with their
  port name.

[core]: /core/

[http_server]: /http_server/
[templates]: /templates/

[http_client_ahc]: /http_client_ahc/
[http_server_jetty]: /http_server_jetty/

# Hexagon Core

The [Hexagon Core][core] module is used by all other libraries, so it would be added to your
project anyway just by using any adapter.

Core utilities like, logging and serialization. Toolkit's ports are designed to use core
functionalities. You can use a third party DI library instead of using the Core one.

The main features are the following:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Objects Serialization]: parse/serialize data in different formats to class instances.

[Helpers]: /api/core/com.hexagonkt.helpers/
[Objects Serialization]: /api/serialization/com.hexagonkt.serialization/

# Other Modules

The following libraries provide extra features not bound to different implementations. They will not
use dependencies outside the Hexagon toolkit.

* [Web]: this module is meant to ease web application development. Provides helpers for
  generating HTML and depends on the [HTTP Server] and [Templates] ports.

[Web]: /web/

# Toolkit Ports

These modules define features that need a specific implementation. You can use many implementations
of each port at the same time. You can even provide a custom implementation if you want to optimize
a particular use case.

These are the implemented ports:

* [HTTP Server]: describes how to use HTTP routing and HTML templates for Web services.
* [HTTP Client]: documentation to use the HTTP client module to connect to other services.
* [Templates]: describes how to render pages using template engines.

[HTTP Server]: /http_server/
[HTTP Client]: /http_client/
[Templates]: /templates/
