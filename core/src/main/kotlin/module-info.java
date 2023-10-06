/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details. You can find a quick recap of the main features in the sections
 * below.
 */
module com.hexagonkt.core {

    requires transitive kotlin.stdlib;

    exports com.hexagonkt.core;
    exports com.hexagonkt.core.logging;
    exports com.hexagonkt.core.media;
    exports com.hexagonkt.core.security;
    exports com.hexagonkt.core.text;

    provides java.net.spi.URLStreamHandlerProvider with com.hexagonkt.core.ClasspathHandlerProvider;
}
