
# HTTP Client Creation Example
This example shows how to create HTTP Client instances. Check the
[full test](https://github.com/hexagontk/hexagon/blob/main/http/http_test/main/examples/ClientTest.kt)
for more information.

## Without setting parameters
@code http/http_test/main/examples/ClientTest.kt?clientCreation

## Using client settings
@code http/http_test/main/examples/ClientTest.kt?clientSettingsCreation

# Send Requests Example
This example shows send HTTP requests to a server. Here you can check the
[full test](https://github.com/hexagontk/hexagon/blob/main/http/http_test/main/examples/ClientTest.kt).

## Generic request
@code http/http_test/main/examples/ClientTest.kt?genericRequest

## Shortcut without body sending
@code http/http_test/main/examples/ClientTest.kt?withoutBodyRequests

## Shortcut with payload sending
@code http/http_test/main/examples/ClientTest.kt?bodyRequests

## Shortcut including body and content type
@code http/http_test/main/examples/ClientTest.kt?bodyAndContentTypeRequests

# Use Cookies Example
Check the details at the [full test](https://github.com/hexagontk/hexagon/blob/main/http/http_test/main/examples/CookiesTest.kt).

@code http/http_test/main/examples/CookiesTest.kt?clientCookies

# Multipart Requests Example
Refer to the [full test](https://github.com/hexagontk/hexagon/blob/main/http/http_test/main/examples/FilesTest.kt)
for more details.

## Send form fields
@code http/http_test/main/examples/MultipartTest.kt?clientForm

## Send and attached file
@code http/http_test/main/examples/MultipartTest.kt?clientFile

# Mutual TLS Example
This example shows how make requests using mutual TLS between the client and the server. You can
check the [full test](https://github.com/hexagontk/hexagon/blob/main/http/http_test/main/examples/HttpsTest.kt)
for more details.

@code http/http_test/main/examples/HttpsTest.kt?https
