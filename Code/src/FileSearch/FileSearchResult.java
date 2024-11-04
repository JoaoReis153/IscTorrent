package FileSearch;

import java.io.Serializable;

public class FileSearchResult implements Serializable {
    private WordSearchMessage searchMessage;
    private String fileName;
    private String hash;
    private int fileSize;
    private String nodeAddress;
    private int port;

    public FileSearchResult(WordSearchMessage searchMessage, String fileName, String hash, int fileSize, String nodeAddress, int port) {
        this.searchMessage = searchMessage;
        this.fileName = fileName;
        this.hash = hash;
        this.fileSize = fileSize;
        this.nodeAddress = nodeAddress;
        this.port = port;
    }

    public WordSearchMessage getSearchMessage() {
        return searchMessage;
    }

    public String getFileName() {
        return fileName;
    }

    public String getHash() {
        return hash;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "FileSearchResult{" +
                "searchMessage=" + searchMessage +
                ", fileName='" + fileName + '\'' +
                ", hash='" + hash + '\'' +
                ", fileSize=" + fileSize +
                ", nodeAddress='" + nodeAddress + '\'' +
                ", port=" + port +
                '}';
    }
}
