# IscTorrent

**A local project for testing a distributed file-sharing system using P2P architecture.**

## Table of Contents
- [Overview](#overview)
- [Features](#features)
  - [Best Features](#best-features)
  - [Basic Features](#basic-features)
- [Prerequisites](#prerequisites)
- [Execution Instructions](#execution-instructions)
- [Usage Example](#usage-example)
- [File Structure](#file-structure)
- [Contributing](#contributing)
- [License](#license)

## Overview
IscTorrent is a project designed to test the functionality of a distributed file-sharing system locally. It consists of multiple nodes that can request files from connected nodes, implementing a peer-to-peer (P2P) architecture. Each node connects directly to others without the need for a central server, allowing for efficient file sharing and testing of ideas.

## Features

### Best Features
- **Simultaneous Download**: Connect to multiple nodes that have the desired file, allowing for parts of the file to be downloaded concurrently.
- **Multi-threading and Synchronization**: Nodes can download files from multiple neighbors while simultaneously responding to requests from other nodes.
- **Safe Download**: Downloads continue until completion, even if one of the nodes goes down during the process.
- **Anti-download Spam**: Prevents duplicate downloads and ensures that files are downloaded only once.

### Basic Features
- **Architecture**: P2P, where each node communicates directly with known nodes without a central server.
- **Connection**: Users can manually connect nodes through the GUI by entering the corresponding port.
- **Search and Download Files**: Search for files in connected nodes and request their download.

## Prerequisites
Before running the project, ensure your system meets the following requirements:

### Software Requirements
- **Java Development Kit (JDK)**
  - **Version**: OpenJDK 11 or higher (tested with OpenJDK 21).
  - **Use**: Required to compile and run Java programs.
  - **How to check**: Run the following command:
    ```bash
    java -version
    ```
  - **Installation**:
    - **Linux (Debian/Ubuntu)**:
      ```bash
      sudo apt update
      sudo apt install openjdk-11-jdk
      java -version
      ```
    - **Mac (Homebrew)**:
      ```bash
      brew install openjdk@11
      brew link --force openjdk@11
      java -version
      ```
    - **Windows**: Download and install the JDK from [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or [OpenJDK](https://openjdk.java.net/).

- **Git**
  - **Version**: Git 2.30 or higher (tested with Git 2.43).
  - **Use**: To clone the repository and manage version control.
  - **How to check**: Run the following command:
    ```bash
    git --version
    ```
  - **Installation**:
    - **Linux (Debian/Ubuntu)**:
      ```bash
      sudo apt update
      sudo apt install git
      git --version
      ```
    - **Mac (Homebrew)**:
      ```bash
      brew install git
      git --version
      ```
    - **Windows**: Download and install Git from [git-scm.com](https://git-scm.com/).

- **Bash Shell**
  - **Version**: Bash 5.0 or higher (tested with Bash 5.2).
  - **Use**: The project uses a `run_project.sh` script that requires a Unix-like shell.
  - **How to check**: Run the following command:
    ```bash
    bash --version
    ```
  - **Installation (Windows only)**: Install Windows Subsystem for Linux (WSL):
    ```powershell
    wsl --install
    ```

## Execution Instructions

### Windows
- The recommended solution is to use Windows Subsystem for Linux (WSL). 

### Linux, Mac, WSL
1. Navigate to the repository folder.
2. Execute the application using the command:
   ```bash
   ./run_project.sh
3. If you encounter an error due to whitespace, run the following command and repeat the process:
   ```bash
   sed -i -e 's/\s*$//' run_project.sh
   ```

## Usage example
1. Start the application in multiple terminals and connect the nodes between each other.
2. Insert a keyword and search for the files of the connected nodes that match it.
3. Select a file and initiate the download.

If you are curious, you can also check the difference between download a file from one node and from multiple to see how faster it can get. 

## Test Project
To make it easier for users to test the program, a test_project.sh script is provided. This script simplifies the process of setting up and running tests on the application. You can execute it by running:
```bash
./test_project.sh
```
This script will make it easier to connect nodes, search for files, download and overall test the application.

## File structure
The project follows a specific file structure. Ensure that the default file structure exists in the `files` directory before running the project.

## Contributing 
Contributions are welcome! Please fork the repository and submit a pull request for any improvements or features.

## License
This project is licensed under the MIT License.