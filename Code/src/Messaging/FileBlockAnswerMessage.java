package Messaging;

import Core.Node;
import Core.Utils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;

public class FileBlockAnswerMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private final int nodeId;
    private final int hash;
    private final long offset;
    private final int length;
    private FileBlockRequestMessage request;
    private byte[] data;

    public FileBlockAnswerMessage(int nodeId, FileBlockRequestMessage request) {
        this.request = request;
        this.nodeId = nodeId;
        this.hash = request.getHash();
        this.offset = request.getOffset();
        this.length = request.getLength();
        if (length <= 0) {
            throw new IllegalArgumentException(
                "Invalid length: length must be positive"
            );
        }
        loadDataFromFile();
    }

    private void loadDataFromFile() {
        try {
            File file = findFileByHash();
            byte[] fileContents = Files.readAllBytes(file.toPath());

            if (offset < 0 || offset + length > fileContents.length) {
                throw new IllegalArgumentException("Invalid offset or length");
            }

            this.data = Arrays.copyOfRange(
                fileContents,
                (int) offset,
                (int) (offset + length)
            );
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            this.data = new byte[0];
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: " + e.getMessage());
            this.data = new byte[0];
        }
    }

    private File findFileByHash() {
        File folder = new File(Node.WORK_FOLDER + nodeId + "/");
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException(
                "Invalid directory path: " + folder.getPath()
            );
        }

        File[] files = folder.listFiles();
        if (files == null) {
            throw new IllegalArgumentException(
                "Unable to list files in directory"
            );
        }

        for (File file : files) {
            if (file.isFile()) {
                if (Utils.calculateFileHash(file.getAbsolutePath()) == hash) {
                    return file;
                }
            }
        }
        throw new IllegalArgumentException("No file found with hash: " + hash);
    }

    public int getHash() {
        return hash;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public FileBlockRequestMessage getBlockRequest() {
        return request;
    }

    @Override
    public String toString() {
        return (
            "FileBlockAnswerMessage{" +
            "nodeId=" +
            nodeId +
            ", hash=" +
            hash +
            ", offset=" +
            offset +
            ", length=" +
            length +
            ", dataSize= " +
            data.length +
            '}'
        );
    }
}
