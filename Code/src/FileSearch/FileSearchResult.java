package FileSearch;

import Core.Node;
import Core.Utils;
import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class FileSearchResult
    implements Serializable, Comparable<FileSearchResult> {

    private WordSearchMessage searchMessage;
    private static final long serialVersionUID = 1L;
    private String fileName;
    private int hash;
    private long fileSize;
    private InetAddress address;
    private int port;

    public FileSearchResult(
        WordSearchMessage searchMessage,
        String fileName,
        int hash,
        long fileSize,
        InetAddress address,
        int port
    ) {
        this.searchMessage = searchMessage;
        this.fileName = fileName;
        this.hash = hash;
        this.fileSize = fileSize;
        this.address = address;
        this.port = port;
    }

    public FileSearchResult(File file, Node node) {
        this.searchMessage = null;
        this.fileName = file.getName();
        this.hash = Utils.calculateFileHash(file.getAbsolutePath());
        this.fileSize = file.length();
        this.address = node.getAddress();
        this.port = node.getPort();
    }

    public WordSearchMessage getSearchMessage() {
        return searchMessage;
    }

    public String getFileName() {
        return fileName;
    }

    public int getHash() {
        return hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public InetAddress getAddress() {
        return address;
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
        return hash == that.hash;
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
