
module com.hexagonkt.http_client_jetty {

    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_client;
    requires transitive org.eclipse.jetty.io;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.client;
    requires transitive org.eclipse.jetty.http2.client;
    requires transitive org.eclipse.jetty.http2.client.transport;

    exports com.hexagonkt.http.client.jetty;
}
