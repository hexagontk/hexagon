/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details.
 */
module com.hexagontk.core {

    requires transitive kotlin.stdlib;

    exports com.hexagontk.core;
    exports com.hexagontk.core.media;
    exports com.hexagontk.core.security;
    exports com.hexagontk.core.text;

    provides java.net.spi.URLStreamHandlerProvider with com.hexagontk.core.ClasspathHandlerProvider;
}
