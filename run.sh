#!/bin/sh

read -p 'Alert requests per second threshold? (Default 10): ' threshold

java -jar target/scala-2.13/logger_app-assembly-0.1.jar $threshold