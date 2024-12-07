#!/bin/bash
sed -i -e 's/\r$//' run_project.sh

PROJECT_DIR="./Code"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
DEFAULT_FILE_STRUCTURE="files"

MAIN_CLASS="Tests.Main"

# Verify default file structure exists
if [ ! -d "$DEFAULT_FILE_STRUCTURE" ]; then
  echo "Default file structure not found: $DEFAULT_FILE_STRUCTURE"
  exit 1
fi

# Remove existing directories starting with 'dl'
find "$PROJECT_DIR" -type d -name "dl*" -exec rm -rf {} +
if [ $? -ne 0 ]; then
  echo "Failed to remove directories starting with 'dl'!"
  exit 1
fi

# Create necessary folders based on arguments
for arg in "$@"; do
  if [ -d "$DEFAULT_FILE_STRUCTURE/dl$arg" ]; then
    cp -r "$DEFAULT_FILE_STRUCTURE/dl$arg" "$PROJECT_DIR"
    if [ $? -ne 0 ]; then
      echo "Failed to copy files for argument: $arg"
      exit 1
    fi
  else
    echo "Argument folder not found in default structure: $arg"
    exit 1
  fi
done

# Compile Java files
javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")
if [ $? -ne 0 ]; then
  echo "Compilation failed!"
  exit 1
fi

# Run the Java main class
java -cp "$BIN_DIR" "$MAIN_CLASS" "$@"
