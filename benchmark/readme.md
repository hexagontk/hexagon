
# Hexagon Benchmarking Test

This is the Hexagon portion of a [benchmarking test suite](../) comparing a variety of web
development platforms. The test utilizes Hexagon routes, Gson for JSON serialization and a custom
OSIV pattern created with Hexagon filters.


## Local setup

### MySql

    mysql -u root -p <db.sql
    
### MongoDB

    tar -Jxvf db.txz && \
    mongorestore dump/ && \
    rm -rf dump
    

## Tests

* [Hexagon application](/src/main/java/hexagon/benchmark/Application.java)


## Infrastructure Software Versions

* [Hexagon 1.1.1](http://there4.co/hexagon)


## Different test setups

* Local environment with Hexagon's built in embedded Jetty (port=8080, context=/)
 * Start application from [Application](/src/main/java/hexagon/benchmark/Application.java)'s main method
* Local environment with Hexagon's built in embedded Undertow (port=8080, context=/)
 * Start application from [Application](/src/main/java/hexagon/benchmark/Application.java)'s main method


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

