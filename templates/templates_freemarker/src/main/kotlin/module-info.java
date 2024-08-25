
module com.hexagontk.templates_freemarker {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.templates;

    requires freemarker;

    exports com.hexagontk.templates.freemarker;
}
