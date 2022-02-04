
module com.hexagonkt.core {

    requires java.base;
    requires java.management;
    requires java.logging;

    requires transitive kotlin.stdlib;

    /*
     * uses com.hexagonkt.core.ClasspathHandlerProvider;
     */
    provides java.net.spi.URLStreamHandlerProvider
         with com.hexagonkt.core.ClasspathHandlerProvider;
}
