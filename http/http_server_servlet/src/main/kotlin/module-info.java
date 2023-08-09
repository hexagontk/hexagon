
module com.hexagonkt.http_server_servlet {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;

    exports com.hexagonkt.http.server.servlet;
}
