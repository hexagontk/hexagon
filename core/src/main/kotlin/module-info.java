
module com.hexagonkt.core {

    requires transitive kotlin.stdlib;

    exports com.hexagonkt.core;
    exports com.hexagonkt.core.logging;
    exports com.hexagonkt.core.media;
    exports com.hexagonkt.core.security;

    provides java.net.spi.URLStreamHandlerProvider with com.hexagonkt.core.ClasspathHandlerProvider;
}
