#!/bin/bash
sed -i -e 's/\r$//' run_project.sh

PROJECT_DIR="./Code"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
DEFAULT_FILE_STRUCTURE="files"

MAIN_CLASS="Tests.Test"

# Verify default file structure exists
if [ ! -d "$DEFAULT_FILE_STRUCTURE" ]; then
  echo "Default file structure not found: $DEFAULT_FILE_STRUCTURE"
  exit 1
fi

# Compile Java files
javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")
if [ $? -ne 0 ]; then
  echo "Compilation failed!"
  exit 1
fi

# Run the Java main class
java -cp "$BIN_DIR" "$MAIN_CLASS" "$@"
