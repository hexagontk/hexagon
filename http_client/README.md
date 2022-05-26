
# Module http_client
This port provides a common interface for using HTTP clients. Many adapters can be developed to use
different technologies.

Its main functionalities are:

* HTTP, HTTPS and [HTTP/2] support
* [Mutual TLS]
* Body encoding/decoding
* Request/response exchange
* Form submissions
* Cookie management
* File uploading/downloading

[HTTP/2]: https://en.wikipedia.org/wiki/HTTP/2
[Mutual TLS]: https://en.wikipedia.org/wiki/Mutual_authentication

### Install the Dependency
This module is not meant to be used directly. You should include an Adapter implementing this
feature (as [http_client_jetty]) in order to create HTTP clients.

[http_client_jetty]: /http_client_jetty

### Create an HTTP client
You create an HTTP Client instance with default options as follows:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?clientCreation

### Settings
If you want to configure options for the client, you can create it with the following code:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?clientSettingsCreation

### Send generic requests
The most common use case is to send a request and get a response. For details about how to
use requests and responses, refer to the [Request] and the [Response] API.

Check this code snippet to get a glimpse on how to send the most general requests:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?genericRequest

[Request]: /api/http_client/com.hexagonkt.http.client/-request
[Response]: /api/http_client/com.hexagonkt.http.client/-response

### Simple requests shortcuts
There are utility methods to make the most common request in an easy way.

#### Without body
@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?withoutBodyRequest

#### With body
@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?bodyRequest

#### With body and content type
@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ClientTest.kt?bodyAndContentTypeRequest

### Cookies
The HTTP client support setting cookies from client side and updates them after any server request.
Check the details in the following code fragment:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/CookiesTest.kt?clientCookies

You can also check the [full test] for more details.

[full test]: https://github.com/hexagonkt/hexagon/blob/master/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/CookiesTest.kt

### Multipart (forms and files)
Using the HTTP client you can send MIME multipart parts to the server. You can use it to post forms
or files.

#### Forms
@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/FilesTest.kt?clientForm

#### Files
@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/FilesTest.kt?clientFile

### TLS
The HTTP client supports server certificates (to use HTTPS and HTTP/2) and also client certificates
(to be able to do mutual TLS). Key stores may have the JKS format (deprecated), or the newer PKCS12
format.

To set up client/server certificates, you need to include [SslSettings] in your [ClientSettings]. In
the sections below you can see how to configure these parameters.

[SslSettings]: /api/http/com.hexagonkt.http/-ssl-settings
[ClientSettings]: /api/http_client/com.hexagonkt.http.client/-client-settings

#### Key Store
This store holds the identity certificate, this certificate is presented to the server by the client
in the handshake for the server to authorize or deny the connection. The following code:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/HttpsTest.kt?keyStoreSettings

#### Trust Store
This key store should include all the trusted certificates. Any certificate added as CA (certificate
authority) makes the client trust any other certificate signed by them. However, you can also add
standalone server certificates.

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/HttpsTest.kt?trustStoreSettings

### Mutual TLS
If you set up the identity (service's own certificate) and the trust store (CAs and servers trusted
by the client), you will achieve double ended authentication (server authenticated by the client,
and client authenticated by the server). You can see a complete example below:

@code http_test/src/main/kotlin/com/hexagonkt/http/test/examples/HttpsTest.kt?https

# Package com.hexagonkt.http.client
This package holds the classes that define the HTTP client and its configuration settings.

# Package com.hexagonkt.http.client.model
TODO
