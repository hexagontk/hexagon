
module com.hexagonkt.templates_pebble {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.templates;

    requires io.pebbletemplates;

    exports com.hexagonkt.templates.pebble;
}
