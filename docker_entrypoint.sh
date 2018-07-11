#!/bin/sh

cd /opt/scylla
ls

export DISPLAY=:1.0
Xvfb :1 -screen 0 1024x768x16 -ac &
java -jar webswing-server.war -c webswing.config -h 0.0.0.0 -p 8080
