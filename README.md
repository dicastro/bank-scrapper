# 0 - Create network (only first time)

```
docker network create bankscrapper
```

# 1 - Start Selenium hub

```
docker run --rm --name selenium-hub -p 4442-4444:4442-4444 --network bankscrapper selenium/hub:4.18.0-20240220
```

# 2 - Start Selenium firefox node

```
docker run --rm --name firefox-node -p 6901:5900 --network bankscrapper -e VNC_NO_PASSWORD=1 -e SE_EVENT_BUS_HOST=selenium-hub -e SE_EVENT_BUS_PUBLISH_PORT=4442 -e SE_EVENT_BUS_SUBSCRIBE_PORT=4443 -e SCREEN_WIDTH=1280 -e SCREEN_HEIGHT=1024 -e SCREEN_DEPTH=24 -e SCREEN_DPI=96 --shm-size=2g selenium/node-firefox:122.0.1-geckodriver-0.34.0-grid-4.18.0-20240220
```

# 3 - Run bankscrapper

build

```
dobker build -f ./docker/Dockerfile -t qopuir/bankscrapper:1.0.0-SNAPSHOT --build-arg BUNDLE_NAME=bankscrapper --build-arg BUNDLE_VERSION=1.0.0-SNAPSHOT . 
```

debug

```
docker run -it --rm --name bankscrapper -p 5005:5005 -e SPRING_PROFILES_ACTIVE=docker -v %HOMEDRIVE%%HOMEPATH%/.bank-scrapper:/wksp --network bankscrapper --entrypoint /bankscrapper/entrypoint_debug.sh qopuir/bankscrapper:1.0.0-SNAPSHOT --sync-type PAST_ONE_MONTH
```

no-debug

```
docker run -it --rm --name bankscrapper -p 5005:5005 -e SPRING_PROFILES_ACTIVE=docker -v %HOMEDRIVE%%HOMEPATH%/.bank-scrapper:/wksp --network bankscrapper qopuir/bankscrapper:1.0.0-SNAPSHOT --sync-type PAST_ONE_MONTH
```