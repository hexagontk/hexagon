#!/bin/bash

fw_depends java

./gradlew
build/install/hexagon/service start
