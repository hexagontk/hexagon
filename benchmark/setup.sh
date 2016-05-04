#!/bin/bash

fw_depends java8

export JAVA_HOME=/opt/java8
mvn clean package -DskipTests -Ddb.host=${DBHOST}
${JAVA_HOME}/bin/java -jar target/hexagon-1.0.0.jar &
