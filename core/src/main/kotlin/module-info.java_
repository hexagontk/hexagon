
module com.hexagonkt.core {

    requires java.management;
    requires java.logging;

    requires transitive kotlin.stdlib;

    exports com.hexagonkt.core;
    exports com.hexagonkt.core.converters;
    exports com.hexagonkt.core.handlers;
    exports com.hexagonkt.core.logging;
    exports com.hexagonkt.core.logging.jul;
    exports com.hexagonkt.core.media;
    exports com.hexagonkt.core.security;

    /*
     * uses com.hexagonkt.core.ClasspathHandlerProvider;
     */
    provides java.net.spi.URLStreamHandlerProvider with com.hexagonkt.core.ClasspathHandlerProvider;
}
