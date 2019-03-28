#!/bin/bash
set -ex

VERSION=$(cat version/version)
export MAVEN_USER_HOME=$(cd maven-cache && pwd)
MAVEN_ARGS="-Dmaven.repo.local=../maven-cache/repository -Drevision=$VERSION"

export MAVEN_ARGS

(
    cd git
    ./mvnw org.jacoco:jacoco-maven-plugin:prepare-agent  install -Dmaven.test.failure.ignore=false $MAVEN_ARGS
    ./mvnw sonar:sonar $MAVEN_ARGS \
        -Dsonar.projectKey=emerald-squad_sonar-shellcheck-plugin \
        -Dsonar.organization=emerald-squad \
        -Dsonar.host.url=https://sonarcloud.io \
        -Dsonar.login=${SONARCLOUD_KEY}

    cp -r target/* ../build-output
)
