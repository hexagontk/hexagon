
# Hexagon Benchmarking Test

This is the Hexagon portion of a [benchmarking test suite](../) comparing a variety of web
development platforms. The test utilizes Hexagon routes and serialization.


## Local setup
    
### MongoDB

    tar -Jxvf db.txz && \
    mongorestore dump/ && \
    rm -rf dump
    

## Tests

* [Hexagon application](/src/main/java/co/there4/hexagon/Benchmark.java)


## Infrastructure Software Versions

* [Hexagon 1.1.1](http://there4.co/hexagon)


## Test URLs

### JSON Encoding Test

http://localhost:5050/json

### Data-Store/Database Mapping Test

http://localhost:5050/db?queries=5

### Plain Text Test

http://localhost:5050/plaintext

### Fortunes

http://localhost:5050/fortune

### Database updates

http://localhost:5050/update

# Copy to TFB

    rm -f db.txz
    
# Run inside vagrant

    toolset/run-tests.py --install server --mode verify --test hexagon
    
# Clear
    

# TODO

Remove `benchmark_config` optional parameters. Check:
http://frameworkbenchmarks.readthedocs.io/en/latest/Codebase/Framework-Files/#benchmark-config-file

Add common commands to test the framework inside Vagrant's development machine
