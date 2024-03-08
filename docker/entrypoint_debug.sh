#!/usr/bin/env sh

echo "JAR_FILE: ${JAR_FILE}"
echo "Args:     $@"

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /bankscrapper/${JAR_FILE} $@