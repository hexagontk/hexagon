
//pluginManagement {
//    repositories {
//        gradlePluginPortal()
//        jcenter()
//    }
//}

rootProject.name = "hexagon"

include(
    // Infrastructure
    "hexagon_site",
    "hexagon_starters",

    // Internal modules
    "hexagon_core",
    "hexagon_http",
    "hexagon_scheduler",
    "hexagon_settings",
    "hexagon_web",

    // Ports
    "port_http_client",
    "port_http_server",
    "port_messaging",
    "port_store",
    "port_templates",

    // Adapters
    "logging_logback",
    "logging_slf4j_jul",
    "serialization_csv",
    "serialization_yaml",
    "serialization_xml",
    "messaging_rabbitmq",
    "http_client_ahc",
    "http_server_servlet",
    "http_server_jetty",
    "store_mongodb",
    "templates_pebble",
    "templates_freemarker"
)
