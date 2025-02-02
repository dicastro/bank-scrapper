#!/usr/bin/env sh

echo "JAR_FILE: ${JAR_FILE}"
echo "JVM_ARGS: ${JVM_ARGS}"
echo "Args:     $@"

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ${JVM_ARGS} -jar /bankscrapper/${JAR_FILE} $@