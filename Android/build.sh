#!/bin/sh
export ANDROID_HOME=/data/opt/android-sdk/
rm -rf build app/build
./gradlew clean assembleRelease
