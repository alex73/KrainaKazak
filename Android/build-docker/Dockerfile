FROM ubuntu:22.04

ARG CMDTOOLS_VERSION=10406996    # from https://developer.android.com/studio#downloads
ARG ANDROID_BUILD_TOOLS_LEVEL=34.0.0
ARG ANDROID_API_LEVEL=34

ENV ANDROID_SDK_ROOT=/opt/android/

ARG DEBIAN_FRONTEND=noninteractive
RUN apt update && apt install -y --no-install-recommends \
    unzip wget openjdk-17-jre-headless

## install android stuff
RUN mkdir -p /opt/android/cmdline-tools
RUN wget -q "https://dl.google.com/android/repository/commandlinetools-linux-${CMDTOOLS_VERSION}_latest.zip" -P /tmp
RUN unzip -q -d /opt/android/cmdline-tools /tmp/commandlinetools-linux-${CMDTOOLS_VERSION}_latest.zip
RUN mv /opt/android/cmdline-tools/cmdline-tools /opt/android/cmdline-tools/latest

RUN yes Y | /opt/android/cmdline-tools/latest/bin/sdkmanager --install "build-tools;${ANDROID_BUILD_TOOLS_LEVEL}" "platforms;android-${ANDROID_API_LEVEL}" "platform-tools"
RUN yes Y | /opt/android/cmdline-tools/latest/bin/sdkmanager --licenses

COPY mykey.keystore /
RUN mkdir /root/.gradle
COPY gradle.properties /root/.gradle/
RUN echo RELEASE_STORE_FILE=/mykey.keystore > /root/.gradle/gradle.properties
RUN echo RELEASE_STORE_PASSWORD=alexsv123 >> /root/.gradle/gradle.properties
RUN echo RELEASE_KEY_ALIAS=alex73 >> /root/.gradle/gradle.properties
RUN echo RELEASE_KEY_PASSWORD=alexsv123 >> /root/.gradle/gradle.properties
