
module com.hexagonkt.templates_jte {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.templates;

    requires gg.jte;
    requires gg.jte.runtime;

    exports com.hexagonkt.templates.jte;
}
