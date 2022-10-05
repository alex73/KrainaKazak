#!/bin/sh

echo Run /app/build.sh
sleep 3

cp ~/gits/My/mykey.keystore .
cp ~/gits/My/gradle.properties .

docker build -t android-build .
docker run -it -v `pwd`/../:/app:rw android-build
