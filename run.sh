#!/bin/sh

read -p 'Requests per Second alerting threshold? (Default 10): ' threshold

java -jar $(dirname "$0")/target/scala-2.13/logger_app-assembly-0.1.jar $threshold