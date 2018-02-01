#!/usr/bin/env bash
basepath=$(cd `dirname $0`/..; pwd)
clientpath=$basepath/test-client/target/test-client-0.0.1-jar-with-dependencies.jar

java -jar $clientpath