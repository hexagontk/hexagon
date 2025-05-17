
module com.hexagontk.serverless_http_google {

    requires transitive com.hexagontk.http_handlers;

    requires functions.framework.api;

    exports com.hexagontk.serverless.http.google;
}
