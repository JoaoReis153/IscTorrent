package FileSearch;

import Core.Utils;
import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class FileSearchResult
    implements Serializable, Comparable<FileSearchResult> {

    private WordSearchMessage searchMessage;
    private static final long serialVersionUID = 1L;
    private String fileName;
    private String hash;
    private long fileSize;
    private String address;
    private int port;

    public FileSearchResult(
        WordSearchMessage searchMessage,
        String fileName,
        String hash,
        long fileSize,
        String address,
        int port
    ) {
        this.searchMessage = searchMessage;
        this.fileName = fileName;
        this.hash = hash;
        this.fileSize = fileSize;
        this.address = address;
        this.port = port;
    }

    public FileSearchResult(String fileName) {
        this.fileName = fileName;
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

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public static FileSearchResult[] getFileSearchResultFromFilesList(
        File[] files,
        String keyword,
        WordSearchMessage obj,
        String endereco,
        int port
    ) {
        if (files.length != 0) {
            int counter = 0;
            FileSearchResult[] results = new FileSearchResult[files.length];

            for (File file : files) {
                String hash = Utils.generateSHA256(file.getAbsolutePath());
                results[counter++] = new FileSearchResult(
                    obj,
                    file.getName(),
                    hash,
                    file.length(),
                    endereco,
                    port
                );
            }
        }
        return new FileSearchResult[0];
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
