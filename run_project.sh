#!/bin/bash

# Define paths for the project
PROJECT_DIR="./code"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/Code/bin"

# Define the main class to run
MAIN_CLASS="Tests.Main"  # Fully qualified name

# Check if an argument is provided
if [ -z "$1" ]; then
    echo "Please provide an ID [0 - 1000]"
    exit 1
fi

# Step 1: Compile the project
echo "Compiling Java files..."
javac -d "$BIN_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")

# Check if compilation was successful
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Step 2: Run the main class with the integer argument
echo "Running the main class with argument $1..."
java -cp "$BIN_DIR" "$MAIN_CLASS" "$1"

