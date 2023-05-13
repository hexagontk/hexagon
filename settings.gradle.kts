
rootProject.name = "hexagon"

include(
    // Infrastructure
    "site",
    "starters",

    // Utility modules
    "core",
    "handlers",
    "handlers_async",
    "http",
    "http_handlers",
    "http_handlers_async",

    // Ports
    "http_client",
    "http_server",
    "http_server_async",
    "serialization",
    "templates",

    // Adapters
    "logging_jul",
    "logging_logback",
    "logging_slf4j_jul",
    "serialization_dsl_json",
    "serialization_jackson",
    "serialization_jackson_json",
    "serialization_jackson_csv",
    "serialization_jackson_toml",
    "serialization_jackson_yaml",
    "serialization_jackson_xml",
    "http_client_jetty",
    "http_client_jetty_ws",
    "http_server_servlet",
    "http_server_jetty",
    "http_server_netty",
    "http_server_netty_epoll",
    "http_server_vertx_async",
    "templates_freemarker",
    "templates_pebble",
    "templates_rocker",

    // Testing
    "http_test",
    "http_test_async",
    "serialization_test",
    "templates_test",
)
