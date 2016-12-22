
# Hexagon Benchmarking Test

This is the Hexagon portion of a [benchmarking test suite](../) comparing a variety of web
development platforms. The test utilizes Hexagon routes, serialization and database access.

## Tests

* [Hexagon application](/src/main/java/co/there4/hexagon/Benchmark.kt)

## Infrastructure Software Versions

* [Hexagon 0.9.11](http://there4.co/hexagon)

## Test URLs

* JSON Encoding Test: http://localhost:9090/json
* Data-Store/Database Mapping Test: http://localhost:9090/db?queries=5 
* Plain Text Test: http://localhost:9090/plaintext 
* Fortunes: http://localhost:9090/fortunes
* Database updates: http://localhost:9090/update
* Database queries: http://localhost:9090/query

## Copy to TFB

Delete `initialize` method
    
## Run inside vagrant

    cd ~/FrameworkBenchmarks
    rm -rf results
    toolset/run-tests.py --install server --mode verify --test hexagon
