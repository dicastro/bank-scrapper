@echo off

docker build -f ./docker/Dockerfile -t qopuir/bankscrapper:1.0.0-SNAPSHOT --build-arg BUNDLE_NAME=bankscrapper --build-arg BUNDLE_VERSION=1.0.0-SNAPSHOT .