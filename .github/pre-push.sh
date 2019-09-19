#!/bin/sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# To clean Docker artifacts execute: `sudo docker system prune -af`
#

set -e

docker-compose stop
docker-compose up -d

./gradlew clean all
./gradlew dokkaMd checkSite

me="$(whoami)"
user="$(id -u "$me"):$(id -g "$me")"
docker run --rm -v "$PWD/hexagon_site:/docs" -u "$user" "squidfunk/mkdocs-material:4.4.2" build
