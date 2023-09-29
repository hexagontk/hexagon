
module com.hexagonkt.http_server_netty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;

    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.transport;

    exports com.hexagonkt.http.server.netty;
}
