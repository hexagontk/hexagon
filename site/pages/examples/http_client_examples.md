
# HTTP Client Creation Example
This example shows how to create HTTP Client instances. Check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/http_client/src/test/kotlin/ClientTest.kt)
for more information.

## Without setting parameters
@code http_test/src/main/kotlin/examples/ClientTest.kt:clientCreation

## Using client settings
@code http_test/src/main/kotlin/examples/ClientTest.kt:clientSettingsCreation

# Send Requests Example
This example shows send HTTP requests to a server. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/http_client/src/test/kotlin/ClientTest.kt).

## Generic request
@code http_test/src/main/kotlin/examples/ClientTest.kt:genericRequest

## Shortcut without body sending
@code http_test/src/main/kotlin/examples/ClientTest.kt:withoutBodyRequests

## Shortcut with payload sending
@code http_test/src/main/kotlin/examples/ClientTest.kt:bodyRequests

## Shortcut including body and content type
@code http_test/src/main/kotlin/examples/ClientTest.kt:bodyAndContentTypeRequests

# Use Cookies Example
Check the details at the [full test](https://github.com/hexagonkt/hexagon/blob/master/http_server/src/test/kotlin/examples/CookiesTest.kt).

@code http_test/src/main/kotlin/examples/CookiesTest.kt:clientCookies

# Multipart Requests Example
Refer to the [full test](https://github.com/hexagonkt/hexagon/blob/master/http_server/src/test/kotlin/examples/FilesTest.kt)
for more details.

## Send form fields
@code http_test/src/main/kotlin/examples/FilesTest.kt:clientForm

## Send and attached file
@code http_test/src/main/kotlin/examples/FilesTest.kt:clientFile

# Mutual TLS Example
This example shows how make requests using mutual TLS between the client and the server. You can
check the [full test](https://github.com/hexagonkt/hexagon/blob/master/http_server/src/test/kotlin/examples/HttpsTest.kt)
for more details.

@code http_test/src/main/kotlin/examples/HttpsTest.kt:https
