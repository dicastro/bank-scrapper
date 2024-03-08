@echo off

docker run --rm --name selenium-hub -p 4442-4444:4442-4444 --network bankscrapper selenium/hub:4.18.1-20240224