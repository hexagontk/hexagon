
module com.hexagonkt.http_server_servlet {

    requires transitive kotlin.stdlib;
    requires transitive java.management;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;
    requires transitive jakarta.servlet;

    exports com.hexagonkt.http.server.servlet;
}
