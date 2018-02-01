#!/usr/bin/env bash
basepath=$(cd `dirname $0`/..; pwd)
serverpath=$basepath/mqtt-server/target/mqtt-server-0.0.1-jar-with-dependencies.jar

java -jar $serverpath