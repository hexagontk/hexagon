
module com.hexagontk.http_client_jetty {

    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_client;
    requires transitive org.eclipse.jetty.io;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.client;
    requires transitive org.eclipse.jetty.http2.client;
    requires transitive org.eclipse.jetty.http2.client.transport;

    exports com.hexagontk.http.client.jetty;
}
