package Messaging;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import Core.Node;
import Core.Utils;

public class FileBlockAnswerMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int nodeId;
    private String hash;
    private long offset;
    private int length;
    private byte[] data;

    public FileBlockAnswerMessage(int nodeId,
        String hash,
        long offset,
        int length
    ) {
    	this.nodeId = nodeId;
        this.hash = hash;
        this.offset = offset;
        this.length = length;
        loadDataFromFile();
    }

    public String getFilename() {
        return hash;
    }

    public String getHash() {
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

    public void loadDataFromFile() {
        File folder = new File(Node.WORK_FOLDER + nodeId + "/");
        if (!folder.isDirectory()) {
            throw new IllegalStateException("The path 'files' is not a directory");
        }

        File matchingFile = null;

        try {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    String fileHash = Utils.generateSHA256(file.getAbsolutePath());
                    if (fileHash.equals(hash)) {
                        matchingFile = file;
                        break;
                    }
                }
            }

            if (matchingFile == null) {
                throw new IllegalArgumentException("No file found with the matching hash: " + hash);
            }

            try (RandomAccessFile raf = new RandomAccessFile(matchingFile, "r")) {
                long fileLength = raf.length();

                // Validate the range
                if (offset < 0 || offset + length > fileLength) {
                    throw new IllegalArgumentException("Invalid range");
                }

                // Read the specified range of bytes
                byte[] data = new byte[length];

                // Move the pointer to the start position
                raf.seek(offset);

                // Read the data into the byte array
                raf.readFully(data);

                this.data = data;
            }

        } catch (IOException e) {
            System.out.println("Error loading data from file: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return (
            "FileBlockAnswerMessage [hash=" +
            hash +
            ", offset=" +
            offset +
            ", length=" +
            length +
            ", data=" +
            data +
            "]"
        );
    }
}
