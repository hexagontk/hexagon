#!/bin/bash

fw_depends java8

export JAVA_HOME=/opt/java8
gradlew clean assemble
tar -xvf build/hexagon-1.0.0.tar -d build
build/hexagon-1.0.0/bin/hexagon
#${JAVA_HOME}/bin/java -jar target/hexagon-1.0.0.jar &
