
module com.hexagonkt.http_server {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.http_handlers;

    exports com.hexagonkt.http.server;
    exports com.hexagonkt.http.server.callbacks;
}
