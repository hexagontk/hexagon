
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
    "serialization_csv",
    "serialization_yaml",
    "messaging_rabbitmq",
    "http_client_ahc",
    "http_server_servlet",
    "http_server_jetty",
    "store_mongodb",
    "templates_pebble",
    "templates_freemarker"
)
