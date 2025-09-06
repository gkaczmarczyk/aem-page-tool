#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")

JAR_PATH="$SCRIPT_DIR/aem-page-tool.jar"

if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java executable not found. Please install Java and ensure it's in your PATH."
    exit 1
fi

JAVA_OPTS="-Xms256m -Xmx1g"

java $JAVA_OPTS -jar "$JAR_PATH" "$@"

if [ $? -ne 0 ]; then
    echo "Error: Execution failed with exit code $?."
    exit 1
fi
