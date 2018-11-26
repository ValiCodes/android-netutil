#!/usr/bin/env bash

./gradlew clean --stacktrace

# upload to Nexus3
#./gradlew :netutil:uploadArchives

# upload to Artifactory
./gradlew :netutil:assembleRelease --stacktrace
./gradlew :netutil:generatePomFileForAarPublication --stacktrace
./gradlew :netutil:artifactoryPublish --stacktrace