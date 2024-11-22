package Messaging;

import Core.Node;
import Core.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private int nodeId;
    private int hash;
    private long offset;
    private int length;
    private byte[] data;

    public FileBlockAnswerMessage(
        int nodeId,
        int hash,
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
            System.err.println("Warning: " + e.getMessage());
            this.data = new byte[0];
        }
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

    public void loadDataFromFile(int hash) {
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
                    int fileHash = Utils.calculateFileHash(
                        file.getAbsolutePath()
                    );
                    if (fileHash == hash) {
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

                if (offset < 0 || offset + length > fileLength) {
                    throw new IllegalArgumentException("Invalid range");
                }

                byte[] data = new byte[length];

                raf.seek(offset);

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
