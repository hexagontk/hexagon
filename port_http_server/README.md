
# Module port_http_server

This port's purpose is to develop HTTP server processes (REST services or Web applications).
It defines a DSL to declare HTTP request handlers.

Adapters implementing this port are responsible of transforming the DSL to a runtime. And allows you
to switch implementations without changing the service.

The [hexagon_web] module provides utilities on top of this port for Web application development
(like template helpers).

[hexagon_web]: /hexagon_web

### Server

A server is a process listening to HTTP requests in a TCP port.

You can run multiple ones on different ports at the same time (this is useful to test many
microservices at the same time).

The server can be configured with different properties. If you do not provide a value for them, they
are searched inside the application settings and lastly, a default value is picked. This is the
parameters list:

* serviceName: name of this service, it is only informative and it is displayed on the logs. If not
  set `<undefined>` is used.
* bindAddress: address to which this process is bound. If none is provided, `127.0.0.1` is taken.
* bindPort: `2010`

You can inject an adapter for the `Server` port using the [InjectionManager]:
`InjectionManager.bindObject<ServerPort>(JettyServletAdapter())`

To create a server, you need to provide a router (check the [next section] for more information),
and after creating a server you can run it or stop it with [start()] and [stop()]

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:serverCreation

[InjectionManager]: /hexagon_core/#dependency-injection
[next section]: /port_http_server/#routes
[start()]: com.hexagonkt.http.server/-server/start.md
[stop()]: com.hexagonkt.http.server/-server/stop.md

#### Servlet Web server

There is an special server adapter for running inside Servlet Containers. To use it you should
import the [Servlet HTTP Server Adapter][http_server_servlet] into your project. Check the
[http_server_servlet] module for more information.

[http_server_servlet]: /http_server_servlet

### Routes

URL trailing slash is ignored and discarded

HTTP clients will be able to reuse the routes to create REST services clients.

The main building block of a Spark application is a set of routes. A route is made up of three
simple pieces:

* A **verb** (get, post, put, delete, head, trace, connect, options). It can also be `any`.
* A **path** (/hello, /users/:name)
* A **callback** (request, response) -> { }

The Handler interface has a void return type. You use ctx.result() to set the response which will be
returned to the user.

Routes are matched in the order they are defined. The first route that matches the request is
invoked:

    CODE

#### Routers

If you have a lot of routes, it can be helpful to group them into routers.

#### Route groups

This can be done by calling the path() method, which takes a String prefix and gives you a scope to
declare routes (or mount routers) and filters (or nested paths) in:

Note that path() prefixes your paths with / (if you don’t add it yourself).
This means that path("api", ...) and path("/api", ...) are equivalent.

### Callbacks

HTTP request handling code block

#### Call

The Call object provides you with everything you need to handle a http-request. It contains the
underlying servlet-request and servlet-response, and a bunch of getters and setters. The getters
operate mostly on the request-object, while the setters operate exclusively on the response object.

#### Request

Request information and functionality is provided by the request parameter:

    CODE

#### Response

Response information and functionality is provided by the response parameter:

    CODE

#### Query Parameters

#### Path Parameters

Route patterns can include named parameters, accessible via the params() method on the request
object:

    CODE

#### Redirects

TODO Implement Spark Java redirect utilities.

#### Cookies

The ctx.cookieStore() functions provide a convenient way for sharing information between handlers,
request, or even servers:

The cookieStore works like this:

1. The first handler that matches the incoming request will populate the cookie-store-map with the
   data currently stored in the cookie (if any).
2. This map can now be used as a state between handlers on the same request-cycle, pretty much in
   the same way as ctx.attribute()
3. At the end of the request-cycle, the cookie-store-map is serialized, base64-encoded and written
   to the response as a cookie. This allows you to share the map between requests and servers (in
   case you are running multiple servers behind a load-balancer)

#### Sessions

Every request has access to the session created on the server side, provided with the following
methods:

#### Halting

To immediately stop a request within a filter or route use halt():

halt() is not intended to be used inside exception-mappers.

### Filters

Before-filters are evaluated before each request, and can read the request and read/modify the
response.

To stop execution, use halt():

After-filters are evaluated after each request, and can read the request and read/modify the
response:

Filters optionally take a pattern, causing them to be evaluated only if the request path matches
that pattern:

Before and after filters are always executed (if the route is matched). But any of them may stop
the execution chain if halted.

You might know before-handlers as filters, interceptors, or middleware from other libraries.

Before-handlers are matched before every request (including static files, if you enable those).

### Error Handling

#### HTTP Errors Handlers

HTTP status codes handling

#### Exception Mapping

To handle exceptions of a configured type for all routes and filters:

### Multipart Requests

#### HTML Form processing

#### File Uploads

### Static Files

You can assign a folder in the classpath serving static files with the staticFiles.location()
method. Note that the public directory name is not included in the URL.

A file /public/css/style.css is made available as http://{host}:{port}/css/style.css

You can also assign an external folder (a folder not in the classpath) to serve static files by
using the staticFiles.externalLocation() method.\

Static files location must be configured before route mapping. If your application has no routes,
init() must be called manually after location is set.

#### MIME types

### Testing

# Package com.hexagonkt.http.server

TODO
