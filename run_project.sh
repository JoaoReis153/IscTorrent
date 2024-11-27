#!/bin/bash
sed -i -e 's/\r$//' run_project.sh

PROJECT_DIR="./Code"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
DEFAULT_FILE_STRUCTURE="files"

MAIN_CLASS="Tests.Main"

if [ -d "$DEFAULT_FILE_STRUCTURE" ]; then
    find "$PROJECT_DIR" -type d -name "dl*" -exec rm -rf {} +
    if [ $? -ne 0 ]; then
        echo "Failed to remove directories starting with 'dl'!"
        exit 1
    fi

    cp -r "$DEFAULT_FILE_STRUCTURE"/* "$PROJECT_DIR"
    if [ $? -ne 0 ]; then
        echo "Failed to copy default files!"
        exit 1
    fi
else
    echo "Default file structure not found: $DEFAULT_FILE_STRUCTURE"
    exit 1
fi

javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

java -cp "$BIN_DIR" "$MAIN_CLASS" "$@"
