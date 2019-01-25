#!/bin/bash
set -ex

VERSION=$(cat version/version)
export MAVEN_USER_HOME=$(cd maven-cache && pwd)
MAVEN_ARGS="-Dmaven.repo.local=../maven-cache/repository -Drevision=$VERSION"
MAVEN_ARGS="$MAVEN_ARGS -Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_KEY}"

export MAVEN_ARGS

(
    cd git
    ./mvnw org.jacoco:jacoco-maven-plugin:prepare-agent  install -Dmaven.test.failure.ignore=false $MAVEN_ARGS
    ./mvnw sonar:sonar $MAVEN_ARGS

    cp -r target/* ../build-output
)
