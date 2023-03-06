#!/bin/sh

echo Run:
echo cd /app/
echo ./build.sh
sleep 5

cp ~/gits/My/mykey.keystore .
cp ~/gits/My/gradle.properties .

docker build -t android-build .
docker run -it -v `pwd`/../:/app:rw android-build
