
module com.hexagontk.http_client_jetty_ws {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http_client_jetty;
    requires transitive com.hexagontk.core;
    requires transitive org.eclipse.jetty.io;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.client;
    requires transitive org.eclipse.jetty.http2.client;
    requires transitive org.eclipse.jetty.http2.client.transport;
    requires transitive org.eclipse.jetty.websocket.api;
    requires transitive org.eclipse.jetty.websocket.client;

    exports com.hexagontk.http.client.jetty.ws;
}
