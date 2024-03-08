@echo off

docker build -f ./docker/Dockerfile -t qopuir/bank-scrapper:1.0.0-SNAPSHOT --build-arg BUNDLE_NAME=bank-scrapper --build-arg BUNDLE_VERSION=1.0.0-SNAPSHOT .