#!/usr/bin/env sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# To clean all Docker artifacts execute: `docker system prune -af`
#

set -e

alias gw='./gradlew --warn --quiet --console plain'
alias d='docker --log-level warning'

# Run all tests
gw clean build

# Clean up
d volume prune -f
d system prune -f
