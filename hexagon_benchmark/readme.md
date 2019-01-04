
# Hexagon Benchmark

This module holds a benchmark for the library. It is the same that runs inside
[TechEmpower Framework Benchmarks][TFB], to run it:

1. Start the benchmark's compose file. From the project's root execute:
   `docker-compose -f docker-compose.yaml -f hexagon_benchmark/docker-compose.yaml up -d`
2. Run `gw hexagon_benchmark:test -Phost=localhost -Pport=9020` where "localhost" and "9020" should
   point to the endpoint with the benchmark instance you want to test.

[TFB]: https://www.techempower.com/benchmarks

## Gatling
  
The stress tests are implemented using Gatling. You can run these tests against any endpoint by
passing configuration properties in the command line (`-D<property>=<value>`).

The allowed benchmark parameters are:

* protocol (string): protocol of the running benchmark server. By default: http
* host (string): host of the running benchmark. By default: 127.0.0.1
* port (int):  port of the benchmark server. By default: 0 (random port)
* databaseEngine (string): database used for the stress tests. By default: mongodb
* templateEngine (string): template engine to be tested. By default: pebble
* users (int): number of users for the load generator. By default: 256
* count (int): times that a benchmark user will repeat a request. By default: 32

On the project's build process this benchmark is run against a server created in the tests, however,
this is not an accurate measure of the performance.

The benchmark reports are generated in `build/benchmarksimulation-<timestamp>` by default, to keep
them among build, another location can be supplied using the `buildDir` system property.

## Benchmark module generation

The `tfb` Gradle task is used generate the files required in the TechEmpower Benchmark project
(`benchmark_config.json` and `*.dockerfiles`). This task can be run using:
`./gradlew hexagon_benchmark:tfb` and the generated files are placed in the `/build/tfb` directory.
