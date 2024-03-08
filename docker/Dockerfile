FROM azul/zulu-openjdk-alpine:21.0.2-21.32-jre

ARG BUNDLE_NAME
ARG BUNDLE_VERSION

RUN apk add tesseract-ocr

WORKDIR /tesseract

RUN mkdir tessdata
COPY ./docker/tesseract/tessdata/eng.traineddata ./tessdata/
RUN mkdir tessconfig
COPY ./docker/tesseract/tessconfig/config ./tessconfig/

WORKDIR /bankscrapper

COPY ./target/$BUNDLE_NAME-$BUNDLE_VERSION.jar .
COPY ./docker/entrypoint.sh .
COPY ./docker/entrypoint_debug.sh .

RUN chmod u+x *.sh

ENV JAR_FILE=$BUNDLE_NAME-$BUNDLE_VERSION.jar

VOLUME /wksp

ENTRYPOINT ["/bankscrapper/entrypoint.sh"]