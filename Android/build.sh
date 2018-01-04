#!/bin/sh

rm -rf build app/build
./gradlew clean assembleRelease
