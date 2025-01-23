
module com.hexagontk.http_server_servlet {

    requires transitive kotlin.stdlib;
    requires transitive java.management;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;
    requires transitive jakarta.servlet;

    exports com.hexagontk.http.server.servlet;
}
