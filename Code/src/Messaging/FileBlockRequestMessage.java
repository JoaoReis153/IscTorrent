package Messaging;


import java.util.ArrayList;
import java.util.List;

public class FileBlockRequestMessage {

    private String hash;
    private long offset;
    private int length;

    public FileBlockRequestMessage(String hash, long offset, int length) {
        this.hash = hash;
        this.offset = offset;
        this.length = length;
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
    
    public static List<FileBlockRequestMessage> createBlockList(String hash, long fileSize) {
    	return createBlockList(hash, fileSize, 10240);
    }

    public static List<FileBlockRequestMessage> createBlockList(String hash, long fileSize, int blockSize) {
        List<FileBlockRequestMessage> blockList = new ArrayList<>();
        long offset = 0;

        while (offset < fileSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockList.add(new FileBlockRequestMessage(hash, offset, length));
            offset += length;
        }

        return blockList;
    }
}

