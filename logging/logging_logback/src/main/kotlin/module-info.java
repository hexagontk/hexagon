/**
 * This module holds utilities used in other libraries of the toolkit. Check the packages'
 * documentation for more details. You can find a quick recap of the main features in the sections
 * below.
 */
module com.hexagonkt.logging_logback {

    requires transitive com.hexagonkt.core;
    requires transitive org.slf4j;
    requires transitive ch.qos.logback.core;
    requires transitive ch.qos.logback.classic;

    exports com.hexagonkt.logging.logback;
}
