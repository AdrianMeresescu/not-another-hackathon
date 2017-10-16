#!/bin/bash
cd "$(dirname "$0")"


# rm -Rf .base/
# sleep 1
# mkdir -p .base/app1
# mkdir -p .base/app2

# cp ./base/Dockerfile .base/Dockerfile
# cp ../../backend/pom.xml .base/app1/pom.xml
# cp ../../proxy/pom.xml .base/app2/pom.xml

# docker build .base/ -t devplant/not-another-hackathon-base

rm -Rf .backend
mkdir .backend
cp -R ../../backend/src .backend/
cp ../../backend/Dockerfile .backend/
cp ../../backend/pom.xml .backend/

docker build --no-cache=true .backend/  -t devplant/not-another-hackathon-backend


rm -Rf .proxy
mkdir .proxy
cp -R ../../proxy/src .proxy/
cp ../../proxy/Dockerfile .proxy/
cp ../../proxy/pom.xml .proxy/

docker build --no-cache=true .proxy/  -t devplant/not-another-hackathon-proxy