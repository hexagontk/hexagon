
module com.hexagontk.web {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;
    requires transitive com.hexagontk.templates;

    exports com.hexagontk.web;
}
