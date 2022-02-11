
module com.hexagonkt.http.server.netty {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.http.server;

    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.codec;
    requires io.netty.codec.http;

    exports com.hexagonkt.http.server.netty;
}
