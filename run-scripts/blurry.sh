#!/bin/sh
# Inspiration: http://www.devx.com/tips/Tip/42153
# IMPORTANT: do not put other jars in the bin directory, or this script might not work

BIN_DIR_PATH=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
JAR_FILENAME=$( ls *.jar | head -n 1 )

java -version:"1.8+" -jar "$BIN_DIR_PATH/$JAR_FILENAME"