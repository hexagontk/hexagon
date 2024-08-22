
module com.hexagontk.http_server {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.http_handlers;

    exports com.hexagontk.http.server;
    exports com.hexagontk.http.server.callbacks;
}
