@echo off

docker run -it --rm --name bankscrapper -p 5005:5005 -e SPRING_PROFILES_ACTIVE=docker,firefox -e JVM_ARGS="-Dbankscrapper.waits.goToStartWait=120s" -v %HOMEDRIVE%%HOMEPATH%/.bank-scrapper:/wksp --network bankscrapper --env-file .env --entrypoint /bankscrapper/entrypoint_debug.sh qopuir/bank-scrapper:1.0.0-SNAPSHOT --user DIE --bank KB --sync-type PAST_ONE_MONTH