
module com.hexagontk.http_server_jdk {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;
    requires transitive jdk.httpserver;

    exports com.hexagontk.http.server.jdk;
}
