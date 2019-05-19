#!/bin/bash

java -Xmx2g -Xms2g -XX:+UseG1GC -jar Bhestie_Tablut.jar $@
