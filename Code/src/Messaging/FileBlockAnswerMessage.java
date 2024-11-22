package Messaging;

import Core.Node;
import Core.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

public class FileBlockAnswerMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int nodeId;
    private String hash;
    private long offset;
    private int length;
    private byte[] data;

    public FileBlockAnswerMessage(
        int nodeId,
        String hash,
        long offset,
        int length
    ) {
        System.out.println("Created FileBlockAnswerMessage");
        this.nodeId = nodeId;
        this.hash = hash;
        this.offset = offset;
        if (length <= 0) {
            System.out.println("ERRO");
            throw new IllegalArgumentException(
                "Error in FileBlockAnswerMessage: File is empty or a file with that lash couldn't be found"
            );
        }
        this.length = length;
        try {
            loadDataFromFile(hash);
        } catch (IllegalArgumentException e) {
            // Log the error and set data to empty or null
            System.err.println("Warning: " + e.getMessage());
            this.data = new byte[0];
        }
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

    public void loadDataFromFile(String hash) {
        File folder = new File(Node.WORK_FOLDER + nodeId + "/");
        if (!folder.isDirectory()) {
            throw new IllegalStateException(
                "The path 'files' is not a directory"
            );
        }

        File matchingFile = null;

        try {
            for (File file : folder.listFiles()) {
                if (file.isFile()) {
                    String fileHash = Utils.generateSHA256(
                        file.getAbsolutePath()
                    );
                    if (fileHash.equals(hash)) {
                        matchingFile = file;
                        break;
                    }
                }
            }

            if (matchingFile == null) {
                throw new IllegalArgumentException(
                    "No file found with the matching hash: " + hash
                );
            }

            try (
                RandomAccessFile raf = new RandomAccessFile(matchingFile, "r")
            ) {
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
            System.out.println(
                "Error loading data from file: " + e.getMessage()
            );
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
