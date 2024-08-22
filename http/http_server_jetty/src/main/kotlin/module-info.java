
module com.hexagontk.http_server_jetty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;
    requires transitive com.hexagontk.http_server_servlet;
    requires transitive jakarta.servlet;
    requires transitive org.eclipse.jetty.alpn.server;
    requires transitive org.eclipse.jetty.ee10.servlet;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.http2.server;

    exports com.hexagontk.http.server.jetty;
}
