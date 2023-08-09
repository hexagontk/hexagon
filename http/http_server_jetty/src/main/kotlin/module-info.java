
module com.hexagonkt.http_server_jetty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;
    requires transitive com.hexagonkt.http_server_servlet;
    requires transitive jetty.servlet.api;
    requires transitive org.eclipse.jetty.alpn.server;
    requires transitive org.eclipse.jetty.servlet;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.http2.server;

    exports com.hexagonkt.http.server.jetty;
}
