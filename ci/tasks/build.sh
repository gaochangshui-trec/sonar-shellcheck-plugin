#!/bin/bash

set -ex

export MAVEN_USER_HOME=$(cd maven-cache && pwd)
export MAVEN_ARGS='-Dmaven.repo.local=../maven-cache/repository'

VERSION=$(<version/version)

pushd git
    ./mvnw install -Drevision=$VERSION $MAVEN_ARGS

    cp target/* ../build-output
    
popd
