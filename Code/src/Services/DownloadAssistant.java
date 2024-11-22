package Services;

import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class DownloadAssistant extends Thread {

    private final DownloadTasksManager taskManager;

    public DownloadAssistant(DownloadTasksManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                handleDownloadRequests();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in DownloadAssistant: " + e);
            }
        }
    }

    private void handleDownloadRequests() throws Exception {
        List<FileSearchResult> request = taskManager.getDownloadRequest();
        if (request == null || request.isEmpty()) return;

        System.out.println("Download Requests: " + request);

        FileSearchResult firstRequest = request.get(0);
        int fileHash = firstRequest.getHash();
        long startTime = System.currentTimeMillis();

        downloadFile(firstRequest);

        long duration = System.currentTimeMillis() - startTime;
        taskManager.getNode().getGUI().showDownloadStats(fileHash, duration);
    }

    private void downloadFile(FileSearchResult fileRequest) throws Exception {
        int fileHash = fileRequest.getHash();
        List<FileBlockRequestMessage> blockList = createBlockRequests(
            fileRequest
        );

        String fileName = fileRequest.getFileName();

        int totalBlocks = blockList.size();

        CountDownLatch latch = new CountDownLatch(totalBlocks);
        distributeBlockRequests(blockList, latch);
        latch.await();

        waitForBlockCompletion(fileName, fileHash, totalBlocks);

        assembleAndWriteFile(
            fileRequest.getFileName(),
            taskManager.getDownloadProcess(fileHash)
        );
    }

    private List<FileBlockRequestMessage> createBlockRequests(
        FileSearchResult fileRequest
    ) {
        return FileBlockRequestMessage.createBlockList(
            fileRequest.getHash(),
            fileRequest.getFileSize()
        );
    }

    private void distributeBlockRequests(
        List<FileBlockRequestMessage> blockList,
        CountDownLatch latch
    ) {
        while (!blockList.isEmpty()) {
            for (SubNode peer : taskManager.getNode().getPeers()) {
                if (blockList.isEmpty()) break;

                FileBlockRequestMessage block = blockList.remove(0);
                if (block == null) continue;

                peer.setBlockAnswerLatch(latch);
                peer.sendFileBlockRequestMessageRequest(block);
            }
        }
    }

    private void waitForBlockCompletion(
        String fileName,
        int fileHash,
        int expectedBlocks
    ) throws IOException {
        while (
            taskManager.getDownloadProcess(fileHash).size() < expectedBlocks
        ) {
            System.out.println(
                taskManager.getDownloadProcess(fileHash).size() +
                " of " +
                expectedBlocks
            );
        }

        System.out.println(
            String.format(
                "Received all blocks: %d of %d",
                taskManager.getDownloadProcess(fileHash).size(),
                expectedBlocks
            )
        );
    }

    private void assembleAndWriteFile(
        String fileName,
        Map<String, ArrayList<FileBlockAnswerMessage>> receivedBlockMap
    ) throws IOException {
        TreeMap<Long, byte[]> fileParts = collectFileParts(receivedBlockMap);
        if (fileParts.isEmpty()) return;
        System.out.println("Running assembleAndWriteFile");
        String filePath = buildFilePath(fileName);
        writeFileToDisc(filePath, fileParts);
        verifyFileCreation(filePath);
    }

    private TreeMap<Long, byte[]> collectFileParts(
        Map<String, ArrayList<FileBlockAnswerMessage>> receivedBlockMap
    ) {
        TreeMap<Long, byte[]> fileParts = new TreeMap<>();
        List<FileBlockAnswerMessage> allBlocks = new ArrayList<>();

        receivedBlockMap.values().forEach(allBlocks::addAll);

        for (FileBlockAnswerMessage block : allBlocks) {
            if (block.getData() == null) {
                System.err.println(
                    "Warning: Block data is null for offset: " +
                    block.getOffset()
                );
                return new TreeMap<>();
            }
            fileParts.put(block.getOffset(), block.getData());
        }

        return fileParts;
    }

    private String buildFilePath(String fileName) {
        return (
            taskManager.getNode().getFolder().getAbsolutePath() + "/" + fileName
        );
    }

    private byte[] combineFileParts(TreeMap<Long, byte[]> fileParts) {
        int totalSize = fileParts
            .values()
            .stream()
            .mapToInt(bytes -> bytes.length)
            .sum();

        byte[] combinedData = new byte[totalSize];
        int position = 0;

        for (byte[] part : fileParts.values()) {
            System.arraycopy(part, 0, combinedData, position, part.length);
            position += part.length;
        }

        return combinedData;
    }

    private void writeFileToDisc(
        String filePath,
        TreeMap<Long, byte[]> fileParts
    ) throws IOException {
        byte[] combinedData = combineFileParts(fileParts);

        Files.write(Paths.get(filePath), combinedData);
    }

    private void verifyFileCreation(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            System.err.println("Error: File was not created at: " + filePath);
            return;
        }
        System.out.println("File written successfully to: " + filePath);
    }
}
