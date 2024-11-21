package FileSearch;

import java.io.Serializable;
import java.util.Objects;

public class FileSearchResult
    implements Serializable, Comparable<FileSearchResult> {

    private WordSearchMessage searchMessage;
    private static final long serialVersionUID = 1L;
    private String fileName;
    private String hash;
    private long fileSize;
    private String nodeAddress;
    private int port;

    public FileSearchResult(
        WordSearchMessage searchMessage,
        String fileName,
        String hash,
        long fileSize,
        String nodeAddress,
        int port
    ) {
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

    public long getFileSize() {
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
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSearchResult that = (FileSearchResult) o;
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public int compareTo(FileSearchResult o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare with null.");
        }
        return this.fileName.compareToIgnoreCase(o.fileName);
    }
}
