#!/bin/sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# To clean Docker artifacts execute: `sudo docker system prune -af`
#

set -e

alias gw='./gradlew --quiet'
alias dc='docker-compose --log-level warning'
alias d='docker --log-level warning'

gw clean installDist -x test

dc -f docker-compose.yml -f hexagon_benchmark/docker-compose.yml rm -sf
dc -f docker-compose.yml -f hexagon_benchmark/docker-compose.yml up -d

gw all
gw dokkaMd checkSite

me="$(whoami)"
user="$(id -u "$me"):$(id -g "$me")"
d run --rm -v "$PWD/hexagon_site:/docs" -u "$user" "squidfunk/mkdocs-material:4.6.0" build
d volume prune -f
