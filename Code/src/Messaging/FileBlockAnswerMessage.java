package Messaging;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {

    private String filePath;
    private String hash;
    private long offset;
    private int length;
    private byte[] data;

    public FileBlockAnswerMessage(
        String filePath,
        String hash,
        long offset,
        int length
    ) {
        this.filePath = filePath;
        this.hash = hash;
        this.offset = offset;
        this.length = length;
    }

    public String getFilename() {
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
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long fileLength = file.length();

            // Validate the range
            if (offset < 0 || offset + length > fileLength) {
                throw new IllegalArgumentException("Invalid range");
            }

            // Calculate the length of data to read
            byte[] data = new byte[(int) length];

            // Move the pointer to the start position
            file.seek(offset);

            // Read the specified range of bytes
            file.readFully(data);

            this.data = data;
        } catch (IOException e) {
            System.out.println("Error loading data from file: " + e);
        }
    }
}
