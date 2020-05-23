
rootProject.name = "hexagon"

gradle.rootProject {

    allprojects {
        version = "1.2.18"
        group = "com.hexagonkt"
        description = "The atoms of your platform"
    }
}

include(
    // Infrastructure
    "hexagon_site",
    "hexagon_starters",

    // Internal modules
    "hexagon_core",
    "hexagon_scheduler",
    "hexagon_web",

    // Ports
    "port_http_client",
    "port_http_server",
    "port_messaging",
    "port_store",
    "port_templates",

    // Adapters
    "messaging_rabbitmq",
    "http_client_ahc",
    "http_server_servlet",
    "http_server_jetty",
    "store_mongodb",
    "templates_pebble"
)
