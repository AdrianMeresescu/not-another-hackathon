#!/bin/bash
cd "$(dirname "$0")"

cd ../webapp
npm install
gulp copy
docker build --no-cache=true . -t devplant/not-another-hackathon-webapp
docker push devplant/not-another-hackathon-webapp

cd "$(dirname "$0")"
mvn -f ../backend/pom.xml install -T 4
mkdir -p .backend
cp ../backend/target/backend-4.2-SNAPSHOT.jar .backend/
cp ../backend/Dockerfile .backend/
docker build --no-cache=true .backend/ -t devplant/not-another-hackathon-backend
docker push devplant/not-another-hackathon-backend

mvn -f ../proxy/pom.xml install -T 4
mkdir -p .proxy
cp ../proxy/target/proxy-4.2-SNAPSHOT.jar .proxy/
cp ../proxy/Dockerfile .proxy/
docker build --no-cache=true .proxy/ -t devplant/not-another-hackathon-proxy
docker push devplant/not-another-hackathon-proxy


