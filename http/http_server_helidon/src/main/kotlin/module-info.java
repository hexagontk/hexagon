
module com.hexagonkt.http_server_helidon {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;
    requires transitive io.helidon.common.parameters;
    requires transitive io.helidon.common.tls;
    requires transitive io.helidon.http;
    requires transitive io.helidon.http.media.multipart;
    requires transitive io.helidon.webserver.http2;

    exports com.hexagonkt.http.server.helidon;
}
