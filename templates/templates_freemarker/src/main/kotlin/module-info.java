
module com.hexagonkt.templates_freemarker {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.templates;

    requires freemarker;

    exports com.hexagonkt.templates.freemarker;
}
