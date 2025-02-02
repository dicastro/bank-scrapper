#!/usr/bin/env sh

echo "JAR_FILE: ${JAR_FILE}"
echo "JVM_ARGS: ${JVM_ARGS}"
echo "Args:     $@"

java ${JVM_ARGS} -jar /bankscrapper/${JAR_FILE} $@