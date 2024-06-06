
module com.hexagonkt.http_client_netty {

    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_client;

    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec.http;
    requires io.netty.codec.http2;

    exports com.hexagonkt.http.client.netty;
}
