
Module port_http_client
=======================
This port provides a common interface for using HTTP clients. Many adapters can be developed to use
different technologies.

Its main functionalities are:

* HTTP, HTTPS and HTTP/2 support
* Mutual TLS
* Body encoding/decoding
* Request/response exchange
* Form submissions
* Cookie management
* File uploading/downloading

### Install the Dependency
This module is not meant to be used directly. You should include and Adapter implementing this
feature (as [http_client_ahc]) in order to create HTTP clients.

[http_client_ahc]: /http_client_ahc

### Create an HTTP client
You create an HTTP Client instance with default options as follows:

@sample port_http_client/src/test/kotlin/ClientTest.kt:clientCreation

### Settings
If you want to configure options for the client, you can create it with the following code:

@sample port_http_client/src/test/kotlin/ClientTest.kt:clientSettingsCreation

### Send generic requests
The most common use case is to send a request and get a response. For details about how to
use requests and responses, refer to the [Request] and the [Response] API.

Check this code snippet to get a glimpse on how to send the most general requests:

@sample port_http_client/src/test/kotlin/ClientTest.kt:genericRequest

[Request]: /port_http_client/com.hexagonkt.http.client/-request
[Response]: /port_http_client/com.hexagonkt.http.client/-response

### Simple requests shortcuts
There are utility methods to make the most common request in an easy way.

#### Without body
@sample port_http_client/src/test/kotlin/ClientTest.kt:withoutBodyRequest

#### With body
@sample port_http_client/src/test/kotlin/ClientTest.kt:bodyRequest

#### With body and content type
@sample port_http_client/src/test/kotlin/ClientTest.kt:bodyAndContentTypeRequest

### Cookies

The HTTP client support setting cookies from client side and updates them after any server request.
Check the details in the following code fragment:

@sample port_http_server/src/test/kotlin/examples/CookiesTest.kt:clientCookies

You can also check the [full test] for more details.

[full test]: https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/examples/CookiesTest.kt

### Multipart (forms and files)

Using the HTTP client you can send MIME multipart parts to the server. You can use it to post forms
or files.

#### Forms
@sample port_http_server/src/test/kotlin/examples/FilesTest.kt:clientForm

#### Files
@sample port_http_server/src/test/kotlin/examples/FilesTest.kt:clientFile

### TLS

#### Key Store

#### Trust Store

### Mutual TLS

### TODO
TODO Handle redirection
TODO Authorization (after being implemented in server)
TODO File handling
TODO Allow sending a list of parts to avoid repeating the part name

Package com.hexagonkt.http.client
=================================
This package holds the classes that define the HTTP client and its configuration settings.
